package ma.youcode.covoiturage.service;

import ma.youcode.covoiturage.dto.PointArretRequest;
import ma.youcode.covoiturage.dto.TrajetRequest;
import ma.youcode.covoiturage.entity.Conducteur;
import ma.youcode.covoiturage.entity.PointArret;
import ma.youcode.covoiturage.entity.Trajet;
import ma.youcode.covoiturage.entity.Vehicule;
import ma.youcode.covoiturage.enums.StatutTrajet;
import ma.youcode.covoiturage.exception.BadRequestException;
import ma.youcode.covoiturage.exception.ConflictException;
import ma.youcode.covoiturage.exception.ForbiddenException;
import ma.youcode.covoiturage.mapper.PointArretMapper;
import ma.youcode.covoiturage.mapper.TrajetMapper;
import ma.youcode.covoiturage.repository.ConducteurRepository;
import ma.youcode.covoiturage.repository.ReservationRepository;
import ma.youcode.covoiturage.repository.TrajetRepository;
import ma.youcode.covoiturage.repository.VehiculeRepository;
import org.junit.jupiter.api.Test;
import ma.youcode.covoiturage.entity.Reservation;
import ma.youcode.covoiturage.enums.StatutReservation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrajetServiceImplTest {

    @Mock
    private TrajetRepository repository;

    @Mock
    private ConducteurRepository conducteurRepository;

    @Mock
    private VehiculeRepository vehiculeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TrajetMapper mapper;

    @Mock
    private PointArretMapper pointArretMapper;

    @InjectMocks
    private TrajetServiceImpl service;

    @Test
    void publier_doitCreerTrajet_quandDonneesValides() {

        // GIVEN
        Conducteur conducteur = conducteur(1L);
        Vehicule vehicule = vehicule(conducteur);

        LocalDateTime depart = LocalDateTime.now()
                .plusDays(7)
                .withNano(0);

        PointArretRequest rabat = new PointArretRequest(
                "Rabat",
                1,
                depart,
                new BigDecimal("0.00")
        );

        PointArretRequest casa = new PointArretRequest(
                "Casablanca",
                2,
                depart.plusHours(1),
                new BigDecimal("40.00")
        );

        TrajetRequest request = new TrajetRequest(
                10L,
                3,
                List.of(rabat, casa)
        );

        when(conducteurRepository.findById(1L))
                .thenReturn(Optional.of(conducteur));

        when(vehiculeRepository.findById(10L))
                .thenReturn(Optional.of(vehicule));

        when(pointArretMapper.toEntity(any(PointArretRequest.class)))
                .thenAnswer(invocation -> {
                    PointArretRequest source = invocation.getArgument(0);

                    PointArret point = new PointArret();
                    point.setVille(source.ville());
                    point.setOrdre(source.ordre());
                    point.setHeurePassage(source.heurePassage());
                    point.setPrixDepuisDepart(source.prixDepuisDepart());

                    return point;
                });

        when(repository.save(any(Trajet.class)))
                .thenAnswer(invocation -> {
                    Trajet trajet = invocation.getArgument(0);
                    trajet.setId(100L);
                    return trajet;
                });

        // WHEN
        service.publier(1L, request);

        // THEN
        ArgumentCaptor<Trajet> captor =
                ArgumentCaptor.forClass(Trajet.class);

        verify(repository).save(captor.capture());

        Trajet saved = captor.getValue();

        assertThat(saved.getConducteur()).isSameAs(conducteur);
        assertThat(saved.getVehicule()).isSameAs(vehicule);
        assertThat(saved.getVilleDepart()).isEqualTo("Rabat");
        assertThat(saved.getVilleArrivee()).isEqualTo("Casablanca");
        assertThat(saved.getDateDepart()).isEqualTo(depart);
        assertThat(saved.getPlacesProposees()).isEqualTo(3);
        assertThat(saved.getStatut()).isEqualTo(StatutTrajet.PUBLIE);

        assertThat(saved.getPointsArret()).hasSize(2);

        assertThat(saved.getPointsArret())
                .allSatisfy(point ->
                        assertThat(point.getTrajet()).isSameAs(saved)
                );
    }

    @Test
    void publier_doitRefuserVehiculeAutreConducteur() {

        // GIVEN
        Conducteur conducteur = conducteur(1L);
        Conducteur autreConducteur = conducteur(2L);
        Vehicule vehicule = vehicule(autreConducteur);

        TrajetRequest request = new TrajetRequest(
                10L,
                3,
                List.of()
        );

        when(conducteurRepository.findById(1L))
                .thenReturn(Optional.of(conducteur));

        when(vehiculeRepository.findById(10L))
                .thenReturn(Optional.of(vehicule));

        // WHEN + THEN
        assertThatThrownBy(() -> service.publier(1L, request))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage(
                        "Ce vehicule n'appartient pas a ce conducteur"
                );

        verify(repository, never()).save(any(Trajet.class));
        verifyNoInteractions(pointArretMapper);
    }

    @Test
    void publier_doitRefuserHeuresNonCroissantes() {

        // GIVEN
        Conducteur conducteur = conducteur(1L);
        Vehicule vehicule = vehicule(conducteur);

        LocalDateTime depart = LocalDateTime.now()
                .plusDays(7);

        TrajetRequest request = new TrajetRequest(
                10L,
                3,
                List.of(
                        new PointArretRequest(
                                "Rabat",
                                1,
                                depart,
                                new BigDecimal("0.00")
                        ),
                        new PointArretRequest(
                                "Casablanca",
                                2,
                                depart,
                                new BigDecimal("40.00")
                        )
                )
        );

        when(conducteurRepository.findById(1L))
                .thenReturn(Optional.of(conducteur));

        when(vehiculeRepository.findById(10L))
                .thenReturn(Optional.of(vehicule));

        // WHEN + THEN
        assertThatThrownBy(() -> service.publier(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(
                        "Les heures de passage doivent etre strictement croissantes"
                );

        verify(repository, never()).save(any(Trajet.class));
        verifyNoInteractions(pointArretMapper);
    }

    @Test
    void terminer_doitRefuserAvantDepart() {

        // GIVEN
        Trajet trajet = new Trajet();
        trajet.setId(100L);
        trajet.setConducteur(conducteur(1L));
        trajet.setStatut(StatutTrajet.PUBLIE);
        trajet.setDateDepart(LocalDateTime.now().plusDays(7));

        when(repository.findByIdForUpdate(100L))
                .thenReturn(Optional.of(trajet));

        // WHEN + THEN
        assertThatThrownBy(() -> service.terminer(1L, 100L))
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "Impossible de terminer un trajet avant son depart"
                );

        assertThat(trajet.getStatut())
                .isEqualTo(StatutTrajet.PUBLIE);

        verifyNoInteractions(mapper);
    }

    private Conducteur conducteur(Long id) {
        Conducteur conducteur = new Conducteur();
        conducteur.setId(id);
        return conducteur;
    }

    private Vehicule vehicule(Conducteur conducteur) {
        Vehicule vehicule = new Vehicule();
        vehicule.setId(10L);
        vehicule.setConducteur(conducteur);
        vehicule.setCapacite(4);
        return vehicule;
    }
    @Test
    void annuler_doitAnnulerTrajetEtToutesReservationsConfirmees() {

        Trajet trajet = new Trajet();
        trajet.setId(100L);
        trajet.setConducteur(conducteur(1L));
        trajet.setStatut(StatutTrajet.COMPLET);

        Reservation premiere = new Reservation();
        premiere.setTrajet(trajet);
        premiere.setStatut(StatutReservation.CONFIRMEE);

        Reservation deuxieme = new Reservation();
        deuxieme.setTrajet(trajet);
        deuxieme.setStatut(StatutReservation.CONFIRMEE);

        when(repository.findByIdForUpdate(100L))
                .thenReturn(Optional.of(trajet));

        when(reservationRepository.findByTrajet_IdAndStatut(
                100L,
                StatutReservation.CONFIRMEE
        )).thenReturn(List.of(premiere, deuxieme));

        service.annuler(1L, 100L);

        assertThat(trajet.getStatut())
                .isEqualTo(StatutTrajet.ANNULE);

        assertThat(premiere.getStatut())
                .isEqualTo(StatutReservation.ANNULEE);

        assertThat(deuxieme.getStatut())
                .isEqualTo(StatutReservation.ANNULEE);
    }
}