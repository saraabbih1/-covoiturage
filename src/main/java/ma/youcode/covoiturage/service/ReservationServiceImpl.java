package ma.youcode.covoiturage.service;

import ma.youcode.covoiturage.dto.ReservationRequest;
import ma.youcode.covoiturage.dto.ReservationResponse;
import ma.youcode.covoiturage.entity.*;
import ma.youcode.covoiturage.enums.StatutReservation;
import ma.youcode.covoiturage.enums.StatutTrajet;
import ma.youcode.covoiturage.exception.BadRequestException;
import ma.youcode.covoiturage.exception.ConflictException;
import ma.youcode.covoiturage.exception.ResourceNotFoundException;
import ma.youcode.covoiturage.mapper.ReservationMapper;
import ma.youcode.covoiturage.repository.PassagerRepository;
import ma.youcode.covoiturage.repository.ReservationRepository;
import ma.youcode.covoiturage.repository.TrajetRepository;
import org.slf4j.Logger;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ma.youcode.covoiturage.exception.ForbiddenException;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log =
            LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository repository;
    private final PassagerRepository passagerRepository;
    private final TrajetRepository trajetRepository;
    private final ReservationMapper mapper;

    public ReservationServiceImpl(
            ReservationRepository repository,
            PassagerRepository passagerRepository,
            TrajetRepository trajetRepository,
            ReservationMapper mapper
    ) {
        this.repository = repository;
        this.passagerRepository = passagerRepository;
        this.trajetRepository = trajetRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ReservationResponse creer(
            Long passagerId,
            ReservationRequest request
    ) {
        Passager passager = passagerRepository.findById(passagerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Passager introuvable : " + passagerId
                        )
                );

        Trajet trajet = trajetRepository
                .findByIdForUpdate(request.trajetId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trajet introuvable : " + request.trajetId()
                        )
                );

        if (trajet.getStatut() != StatutTrajet.PUBLIE) {
            throw new ConflictException(
                    "Ce trajet n'est pas ouvert aux reservations"
            );
        }

        if (!trajet.getDateDepart().isAfter(LocalDateTime.now())) {
            throw new ConflictException(
                    "Impossible de reserver un trajet deja parti"
            );
        }

        PointArret montee = trouverPoint(
                trajet,
                request.pointMonteeId()
        );

        PointArret descente = trouverPoint(
                trajet,
                request.pointDescenteId()
        );

        if (montee.getOrdre() >= descente.getOrdre()) {
            throw new BadRequestException(
                    "Le point de montee doit preceder le point de descente"
            );
        }

        if (repository.existsByPassager_IdAndTrajet_IdAndStatut(
                passagerId,
                trajet.getId(),
                StatutReservation.CONFIRMEE)) {

            throw new ConflictException(
                    "Ce passager a deja une reservation confirmee sur ce trajet"
            );
        }

        long placesReservees = repository.compterPlacesReservees(
                trajet.getId(),
                StatutReservation.CONFIRMEE
        );

        long placesRestantes =
                trajet.getPlacesProposees() - placesReservees;

        if (request.nombrePlaces() > placesRestantes) {
            throw new ConflictException(
                    "Nombre de places disponibles insuffisant"
            );
        }

        BigDecimal prixParPlace = descente.getPrixDepuisDepart()
                .subtract(montee.getPrixDepuisDepart());

        BigDecimal montantTotal = prixParPlace
                .multiply(BigDecimal.valueOf(request.nombrePlaces()))
                .setScale(2, RoundingMode.UNNECESSARY);

        Reservation reservation = new Reservation();
        reservation.setPassager(passager);
        reservation.setTrajet(trajet);
        reservation.setPointMontee(montee);
        reservation.setPointDescente(descente);
        reservation.setNombrePlaces(request.nombrePlaces());
        reservation.setMontantTotal(montantTotal);
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setStatut(StatutReservation.CONFIRMEE);

        Reservation saved = repository.save(reservation);

        if (placesRestantes == request.nombrePlaces()) {
            trajet.setStatut(StatutTrajet.COMPLET);
        }

        log.info(
                "Reservation creee : id={}, passagerId={}, trajetId={}, places={}",
                saved.getId(),
                passagerId,
                trajet.getId(),
                saved.getNombrePlaces()
        );

        return mapper.toResponse(saved);
    }

    private PointArret trouverPoint(Trajet trajet, Long pointId) {
        return trajet.getPointsArret()
                .stream()
                .filter(point -> point.getId().equals(pointId))
                .findFirst()
                .orElseThrow(() ->
                        new BadRequestException(
                                "Le point " + pointId
                                        + " n'appartient pas au trajet"
                        )
                );
    }
    @Override
    @Transactional
    public void annuler(Long passagerId, Long reservationId) {

        Reservation reservation = repository.findById(reservationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Reservation introuvable : " + reservationId
                        )
                );

        if (!reservation.getPassager().getId().equals(passagerId)) {
            throw new ForbiddenException(
                    "Cette reservation n'appartient pas a ce passager"
            );
        }

        // Même verrou que celui utilisé lors de la création.
        Trajet trajet = trajetRepository
                .findByIdForUpdate(reservation.getTrajet().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Trajet introuvable")
                );

        // Relire la réservation après acquisition du verrou.
        // findById seul pourrait réutiliser une ancienne valeur en mémoire.
        repository.flush();
        actualiserReservation(reservation);

        if (reservation.getStatut() != StatutReservation.CONFIRMEE) {
            throw new ConflictException(
                    "Cette reservation est deja annulee"
            );
        }

        if (trajet.getStatut() != StatutTrajet.PUBLIE
                && trajet.getStatut() != StatutTrajet.COMPLET) {
            throw new ConflictException(
                    "Impossible d'annuler sur un trajet termine ou annule"
            );
        }

        LocalDateTime limite = trajet.getDateDepart().minusHours(24);

        if (LocalDateTime.now().isAfter(limite)) {
            throw new ConflictException(
                    "L'annulation doit avoir lieu au moins 24 heures avant le depart"
            );
        }

        reservation.setStatut(StatutReservation.ANNULEE);

        if (trajet.getStatut() == StatutTrajet.COMPLET) {
            trajet.setStatut(StatutTrajet.PUBLIE);
        }

        log.info(
                "Reservation annulee : id={}, passagerId={}, trajetId={}",
                reservationId,
                passagerId,
                trajet.getId()
        );
    }
    @PersistenceContext
    private EntityManager entityManager;

    private void actualiserReservation(Reservation reservation) {
        entityManager.refresh(reservation);
    }
    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> historique(Long passagerId) {

        if (!passagerRepository.existsById(passagerId)) {
            throw new ResourceNotFoundException(
                    "Passager introuvable : " + passagerId
            );
        }

        return repository.findHistoriqueByPassagerId(passagerId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}