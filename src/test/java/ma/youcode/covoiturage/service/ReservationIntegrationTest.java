package ma.youcode.covoiturage.service;

import ma.youcode.covoiturage.dto.ReservationRequest;
import ma.youcode.covoiturage.exception.ConflictException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:postgresql://localhost:5434/covoiturage_test",
                "spring.datasource.username=cov_user",
                "spring.datasource.password=cov_local",
                "spring.jpa.hibernate.ddl-auto=update"
        }
)
class ReservationIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private TrajetService trajetService;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long conducteurId;
    private Long vehiculeId;
    private Long trajetId;
    private Long monteeId;
    private Long descenteId;
    private Long passagerAId;
    private Long passagerBId;

    @BeforeEach
    void preparerDonnees() {

        String token = UUID.randomUUID().toString();

        conducteurId = inserer("""
                INSERT INTO conducteurs
                    (nom, prenom, email, telephone, numero_permis)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """,
                "Test",
                "Conducteur",
                token + "@example.com",
                "0600000000",
                token
        );

        vehiculeId = inserer("""
                INSERT INTO vehicules
                    (marque, modele, immatriculation,
                     couleur, capacite, conducteur_id)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                "Dacia",
                "Logan",
                token,
                "Blanc",
                4,
                conducteurId
        );

        passagerAId = creerPassager("A", token);
        passagerBId = creerPassager("B", token);

        LocalDateTime depart = LocalDateTime.now()
                .plusDays(7)
                .withNano(0);

        trajetId = inserer("""
                INSERT INTO trajets
                    (conducteur_id, vehicule_id,
                     ville_depart, ville_arrivee,
                     date_depart, places_proposees, statut)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                conducteurId,
                vehiculeId,
                "Rabat",
                "Marrakech",
                depart,
                1,
                "PUBLIE"
        );

        monteeId = inserer("""
                INSERT INTO points_arret
                    (trajet_id, ville, ordre,
                     heure_passage, prix_depuis_depart)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """,
                trajetId,
                "Rabat",
                1,
                depart,
                0
        );

        descenteId = inserer("""
                INSERT INTO points_arret
                    (trajet_id, ville, ordre,
                     heure_passage, prix_depuis_depart)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """,
                trajetId,
                "Marrakech",
                2,
                depart.plusHours(4),
                120
        );
    }

    @AfterEach
    void nettoyerDonnees() {

        if (trajetId != null) {
            jdbc.update(
                    "DELETE FROM reservations WHERE trajet_id = ?",
                    trajetId
            );

            jdbc.update(
                    "DELETE FROM points_arret WHERE trajet_id = ?",
                    trajetId
            );

            jdbc.update(
                    "DELETE FROM trajets WHERE id = ?",
                    trajetId
            );
        }

        if (vehiculeId != null) {
            jdbc.update(
                    "DELETE FROM vehicules WHERE id = ?",
                    vehiculeId
            );
        }

        if (conducteurId != null) {
            jdbc.update(
                    "DELETE FROM conducteurs WHERE id = ?",
                    conducteurId
            );
        }

        if (passagerAId != null) {
            jdbc.update(
                    "DELETE FROM passagers WHERE id = ?",
                    passagerAId
            );
        }

        if (passagerBId != null) {
            jdbc.update(
                    "DELETE FROM passagers WHERE id = ?",
                    passagerBId
            );
        }
    }

    @Test
    void deuxReservationsConcurrentes_uneSeuleDoitReussir()
            throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch prets = new CountDownLatch(2);
        CountDownLatch depart = new CountDownLatch(1);

        try {
            Future<Boolean> premier = executor.submit(() ->
                    tenterReservation(passagerAId, prets, depart)
            );

            Future<Boolean> deuxieme = executor.submit(() ->
                    tenterReservation(passagerBId, prets, depart)
            );

            assertThat(prets.await(10, TimeUnit.SECONDS)).isTrue();

            // Les deux threads peuvent maintenant réserver.
            depart.countDown();

            boolean resultatA = premier.get(20, TimeUnit.SECONDS);
            boolean resultatB = deuxieme.get(20, TimeUnit.SECONDS);

            int nombreSucces =
                    (resultatA ? 1 : 0) + (resultatB ? 1 : 0);

            assertThat(nombreSucces).isEqualTo(1);

            Long placesConfirmees = jdbc.queryForObject("""
                    SELECT COALESCE(SUM(nombre_places), 0)
                    FROM reservations
                    WHERE trajet_id = ?
                      AND statut = 'CONFIRMEE'
                    """,
                    Long.class,
                    trajetId
            );

            assertThat(placesConfirmees).isEqualTo(1L);
            assertThat(statutTrajet()).isEqualTo("COMPLET");

        } finally {
            depart.countDown();
            executor.shutdownNow();
            executor.awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    @Test
    void annulation_doitEtreAnnuleeSiTransactionEchoue() {

        reservationService.creer(
                passagerAId,
                nouvelleReservation()
        );

        assertThat(statutTrajet()).isEqualTo("COMPLET");

        TransactionTemplate transaction =
                new TransactionTemplate(transactionManager);

        assertThatThrownBy(() ->
                transaction.executeWithoutResult(status -> {

                    trajetService.annuler(conducteurId, trajetId);

                    // Envoyer les changements en base avant l'échec.
                    // Ils doivent tout de même être annulés par rollback.
                    transactionManagerFlush();

                    throw new IllegalStateException("Echec simule");
                })
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Echec simule");

        // Vérifier les données après le rollback.
        assertThat(statutTrajet()).isEqualTo("COMPLET");

        String statutReservation = jdbc.queryForObject("""
                SELECT statut
                FROM reservations
                WHERE trajet_id = ?
                  AND passager_id = ?
                """,
                String.class,
                trajetId,
                passagerAId
        );

        assertThat(statutReservation).isEqualTo("CONFIRMEE");
    }

    private void transactionManagerFlush() {
        org.springframework.transaction.interceptor
                .TransactionAspectSupport.class.getName();

        // Le flush est effectué sur l'EntityManager
        // lié à la transaction courante.
        entityManager.flush();
    }

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private boolean tenterReservation(
            Long passagerId,
            CountDownLatch prets,
            CountDownLatch depart
    ) throws InterruptedException {

        prets.countDown();

        if (!depart.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException(
                    "Delai de demarrage depasse"
            );
        }

        try {
            reservationService.creer(
                    passagerId,
                    nouvelleReservation()
            );

            return true;

        } catch (ConflictException exception) {
            return false;
        }
    }

    private ReservationRequest nouvelleReservation() {
        return new ReservationRequest(
                trajetId,
                monteeId,
                descenteId,
                1
        );
    }

    private String statutTrajet() {
        return jdbc.queryForObject(
                "SELECT statut FROM trajets WHERE id = ?",
                String.class,
                trajetId
        );
    }

    private Long creerPassager(String suffixe, String token) {
        return inserer("""
                INSERT INTO passagers
                    (nom, prenom, email, telephone)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """,
                "Test",
                suffixe,
                suffixe + token + "@example.com",
                "0600000000"
        );
    }

    private Long inserer(String sql, Object... arguments) {
        return jdbc.queryForObject(sql, Long.class, arguments);
    }
    @Test
    void terminer_doitEnregistrerStatutTermineApresDepart() {

        LocalDateTime departPasse = LocalDateTime.now().minusHours(1);

        jdbc.update(
                "UPDATE trajets SET date_depart = ? WHERE id = ?",
                departPasse,
                trajetId
        );

        trajetService.terminer(conducteurId, trajetId);

        assertThat(statutTrajet()).isEqualTo("TERMINE");

        // Un trajet terminé ne peut plus être annulé.
        assertThatThrownBy(() ->
                trajetService.annuler(conducteurId, trajetId)
        ).isInstanceOf(ConflictException.class);

        assertThat(statutTrajet()).isEqualTo("TERMINE");
    }
}