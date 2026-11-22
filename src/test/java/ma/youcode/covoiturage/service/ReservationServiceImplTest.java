package ma.youcode.covoiturage.service;

import ma.youcode.covoiturage.dto.ReservationRequest;
import ma.youcode.covoiturage.entity.Passager;
import ma.youcode.covoiturage.entity.PointArret;
import ma.youcode.covoiturage.entity.Reservation;
import ma.youcode.covoiturage.entity.Trajet;
import ma.youcode.covoiturage.enums.StatutReservation;
import ma.youcode.covoiturage.enums.StatutTrajet;
import ma.youcode.covoiturage.exception.ConflictException;
import ma.youcode.covoiturage.mapper.ReservationMapper;
import ma.youcode.covoiturage.repository.PassagerRepository;
import ma.youcode.covoiturage.repository.ReservationRepository;
import ma.youcode.covoiturage.repository.TrajetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import jakarta.persistence.EntityManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository repository;

    @Mock
    private PassagerRepository passagerRepository;

    @Mock
    private TrajetRepository trajetRepository;

    @Mock
    private ReservationMapper mapper;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ReservationServiceImpl service;

    @Test
    void creer_doitCalculerMontantEtCompleterTrajet() {

        // GIVEN : trois places proposées, une déjà réservée.
        Trajet trajet = preparerTrajet();

        ReservationRequest request =
                new ReservationRequest(1L, 3L, 4L, 2);

        when(passagerRepository.findById(1L))
                .thenReturn(Optional.of(new Passager()));

        when(trajetRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(trajet));

        when(repository.compterPlacesReservees(
                1L, StatutReservation.CONFIRMEE))
                .thenReturn(1L);

        when(repository.save(any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation reservation = invocation.getArgument(0);
                    reservation.setId(10L);
                    return reservation;
                });

        // WHEN
        service.creer(1L, request);

        // THEN : (120 - 70) × 2 = 100 DH.
        ArgumentCaptor<Reservation> captor =
                ArgumentCaptor.forClass(Reservation.class);

        verify(repository).save(captor.capture());

        Reservation reservation = captor.getValue();

        assertThat(reservation.getMontantTotal())
                .isEqualByComparingTo("100.00");

        assertThat(reservation.getNombrePlaces()).isEqualTo(2);

        assertThat(reservation.getStatut())
                .isEqualTo(StatutReservation.CONFIRMEE);

        assertThat(reservation.getTrajet()).isSameAs(trajet);

        assertThat(trajet.getStatut())
                .isEqualTo(StatutTrajet.COMPLET);
    }

    @Test
    void creer_doitRefuserQuandPlacesInsuffisantes() {

        // GIVEN : il reste une place, mais on en demande deux.
        Trajet trajet = preparerTrajet();

        ReservationRequest request =
                new ReservationRequest(1L, 3L, 4L, 2);

        when(passagerRepository.findById(1L))
                .thenReturn(Optional.of(new Passager()));

        when(trajetRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(trajet));

        when(repository.compterPlacesReservees(
                1L, StatutReservation.CONFIRMEE))
                .thenReturn(2L);

        // WHEN + THEN
        assertThatThrownBy(() -> service.creer(1L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Nombre de places disponibles insuffisant");

        verify(repository, never()).save(any(Reservation.class));

        assertThat(trajet.getStatut())
                .isEqualTo(StatutTrajet.PUBLIE);
    }

    private Trajet preparerTrajet() {

        Trajet trajet = new Trajet();
        trajet.setId(1L);
        trajet.setStatut(StatutTrajet.PUBLIE);
        trajet.setPlacesProposees(3);
        trajet.setDateDepart(LocalDateTime.now().plusDays(7));

        PointArret montee = new PointArret();
        montee.setId(3L);
        montee.setOrdre(3);
        montee.setPrixDepuisDepart(new BigDecimal("70.00"));

        PointArret descente = new PointArret();
        descente.setId(4L);
        descente.setOrdre(4);
        descente.setPrixDepuisDepart(new BigDecimal("120.00"));

        trajet.ajouterPointArret(montee);
        trajet.ajouterPointArret(descente);

        return trajet;
    }
    @Test
    void creer_doitRefuserReservationConfirmeeDupliquee() {

        Trajet trajet = preparerTrajet();

        ReservationRequest request =
                new ReservationRequest(1L, 3L, 4L, 1);

        when(passagerRepository.findById(1L))
                .thenReturn(Optional.of(new Passager()));

        when(trajetRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(trajet));

        when(repository.existsByPassager_IdAndTrajet_IdAndStatut(
                1L,
                1L,
                StatutReservation.CONFIRMEE
        )).thenReturn(true);

        assertThatThrownBy(() -> service.creer(1L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "Ce passager a deja une reservation confirmee sur ce trajet"
                );

        verify(repository, never()).save(any(Reservation.class));
    }

    @Test
    void annuler_doitAnnulerReservationEtReouvrirTrajet() {

        ReflectionTestUtils.setField(
                service,
                "entityManager",
                entityManager
        );

        Trajet trajet = preparerTrajet();
        trajet.setStatut(StatutTrajet.COMPLET);

        Passager passager = new Passager();
        passager.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setId(10L);
        reservation.setPassager(passager);
        reservation.setTrajet(trajet);
        reservation.setNombrePlaces(1);
        reservation.setStatut(StatutReservation.CONFIRMEE);

        when(repository.findById(10L))
                .thenReturn(Optional.of(reservation));

        when(trajetRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(trajet));

        service.annuler(1L, 10L);

        assertThat(reservation.getStatut())
                .isEqualTo(StatutReservation.ANNULEE);

        assertThat(trajet.getStatut())
                .isEqualTo(StatutTrajet.PUBLIE);

        verify(entityManager).refresh(reservation);
    }

    @Test
    void annuler_doitRefuserMoinsDe24HeuresAvantDepart() {

        ReflectionTestUtils.setField(
                service,
                "entityManager",
                entityManager
        );

        Trajet trajet = preparerTrajet();
        trajet.setDateDepart(LocalDateTime.now().plusHours(2));
        trajet.setStatut(StatutTrajet.COMPLET);

        Passager passager = new Passager();
        passager.setId(1L);

        Reservation reservation = new Reservation();
        reservation.setId(10L);
        reservation.setPassager(passager);
        reservation.setTrajet(trajet);
        reservation.setStatut(StatutReservation.CONFIRMEE);

        when(repository.findById(10L))
                .thenReturn(Optional.of(reservation));

        when(trajetRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(trajet));

        assertThatThrownBy(() -> service.annuler(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "L'annulation doit avoir lieu au moins 24 heures avant le depart"
                );

        assertThat(reservation.getStatut())
                .isEqualTo(StatutReservation.CONFIRMEE);

        assertThat(trajet.getStatut())
                .isEqualTo(StatutTrajet.COMPLET);
    }
}