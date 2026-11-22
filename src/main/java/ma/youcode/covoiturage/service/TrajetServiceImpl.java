package ma.youcode.covoiturage.service;

import ma.youcode.covoiturage.dto.PointArretRequest;
import ma.youcode.covoiturage.dto.TrajetRequest;
import ma.youcode.covoiturage.dto.TrajetResponse;
import ma.youcode.covoiturage.entity.Conducteur;
import ma.youcode.covoiturage.entity.PointArret;
import ma.youcode.covoiturage.entity.Trajet;
import ma.youcode.covoiturage.entity.Vehicule;
import ma.youcode.covoiturage.enums.StatutTrajet;
import ma.youcode.covoiturage.exception.BadRequestException;
import ma.youcode.covoiturage.exception.ForbiddenException;
import ma.youcode.covoiturage.exception.ResourceNotFoundException;
import ma.youcode.covoiturage.mapper.PointArretMapper;
import ma.youcode.covoiturage.mapper.TrajetMapper;
import ma.youcode.covoiturage.repository.ConducteurRepository;
import ma.youcode.covoiturage.repository.TrajetRepository;
import ma.youcode.covoiturage.repository.VehiculeRepository;
import org.slf4j.Logger;
import ma.youcode.covoiturage.exception.ConflictException;
import ma.youcode.covoiturage.dto.TrajetRechercheResponse;
import ma.youcode.covoiturage.enums.StatutReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class TrajetServiceImpl implements TrajetService {

    private static final Logger log =
            LoggerFactory.getLogger(TrajetServiceImpl.class);

    private final TrajetRepository repository;
    private final ConducteurRepository conducteurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TrajetMapper mapper;
    private final PointArretMapper pointArretMapper;

    public TrajetServiceImpl(
            TrajetRepository repository,
            ConducteurRepository conducteurRepository,
            VehiculeRepository vehiculeRepository,
            TrajetMapper mapper,
            PointArretMapper pointArretMapper
    ) {
        this.repository = repository;
        this.conducteurRepository = conducteurRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.mapper = mapper;
        this.pointArretMapper = pointArretMapper;
    }

    @Override
    @Transactional
    public TrajetResponse publier(
            Long conducteurId,
            TrajetRequest request
    ) {
        Conducteur conducteur = conducteurRepository.findById(conducteurId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conducteur introuvable : " + conducteurId
                        )
                );

        Vehicule vehicule = vehiculeRepository.findById(request.vehiculeId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehicule introuvable : " + request.vehiculeId()
                        )
                );

        if (!vehicule.getConducteur().getId().equals(conducteurId)) {
            throw new ForbiddenException(
                    "Ce vehicule n'appartient pas a ce conducteur"
            );
        }

        if (request.placesProposees() < 1
                || request.placesProposees() > vehicule.getCapacite()) {

            throw new BadRequestException(
                    "Le nombre de places doit etre compris entre 1 et "
                            + vehicule.getCapacite()
            );
        }

        List<PointArretRequest> points = request.pointsArret()
                .stream()
                .sorted(Comparator.comparing(PointArretRequest::ordre))
                .toList();

        verifierPoints(points);

        PointArretRequest depart = points.get(0);
        PointArretRequest arrivee = points.get(points.size() - 1);

        Trajet trajet = new Trajet();
        trajet.setConducteur(conducteur);
        trajet.setVehicule(vehicule);
        trajet.setVilleDepart(depart.ville().trim());
        trajet.setVilleArrivee(arrivee.ville().trim());
        trajet.setDateDepart(depart.heurePassage());
        trajet.setPlacesProposees(request.placesProposees());
        trajet.setStatut(StatutTrajet.PUBLIE);

        for (PointArretRequest pointRequest : points) {
            PointArret point = pointArretMapper.toEntity(pointRequest);
            point.setVille(pointRequest.ville().trim());
            trajet.ajouterPointArret(point);
        }

        Trajet saved = repository.save(trajet);

        log.info(
                "Trajet publie : id={}, conducteurId={}, places={}",
                saved.getId(),
                conducteurId,
                saved.getPlacesProposees()
        );

        return mapper.toResponse(saved);
    }

    private void verifierPoints(List<PointArretRequest> points) {

        if (points.size() < 2) {
            throw new BadRequestException(
                    "Il faut au moins deux points d'arret"
            );
        }

        PointArretRequest depart = points.get(0);

        if (!depart.heurePassage().isAfter(LocalDateTime.now())) {
            throw new BadRequestException(
                    "La date de depart doit etre dans le futur"
            );
        }

        if (depart.prixDepuisDepart().compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException(
                    "Le prix du premier point doit etre egal a zero"
            );
        }

        Set<String> villes = new HashSet<>();

        for (int i = 0; i < points.size(); i++) {

            PointArretRequest point = points.get(i);

            if (point.ordre() != i + 1) {
                throw new BadRequestException(
                        "Les ordres doivent etre consecutifs a partir de 1"
                );
            }

            String ville = point.ville()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (!villes.add(ville)) {
                throw new BadRequestException(
                        "Une ville ne peut pas apparaitre deux fois"
                );
            }

            if (i > 0) {
                PointArretRequest precedent = points.get(i - 1);

                if (!point.heurePassage()
                        .isAfter(precedent.heurePassage())) {

                    throw new BadRequestException(
                            "Les heures de passage doivent etre "
                                    + "strictement croissantes"
                    );
                }

                if (point.prixDepuisDepart()
                        .compareTo(precedent.prixDepuisDepart()) <= 0) {

                    throw new BadRequestException(
                            "Les prix doivent etre strictement croissants"
                    );
                }
            }
        }
    }
    @Override
    @Transactional(readOnly = true)
    public TrajetResponse consulter(Long id) {

        Trajet trajet = repository.findDetailById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trajet introuvable : " + id
                        )
                );

        return mapper.toResponse(trajet);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<TrajetRechercheResponse> rechercher(
            String depart,
            String arrivee,
            LocalDate date,
            Integer places,
            BigDecimal prixMax,
            int page,
            int size
    ) {
        if (depart == null || depart.isBlank()
                || arrivee == null || arrivee.isBlank()) {

            throw new BadRequestException(
                    "Les villes de depart et d'arrivee sont obligatoires"
            );
        }

        String villeDepart = depart.trim();
        String villeArrivee = arrivee.trim();

        if (villeDepart.equalsIgnoreCase(villeArrivee)) {
            throw new BadRequestException(
                    "Les villes de depart et d'arrivee doivent etre differentes"
            );
        }

        if (date == null || date.isBefore(LocalDate.now())) {
            throw new BadRequestException(
                    "La date est obligatoire et ne doit pas etre dans le passe"
            );
        }

        if (places == null || places < 1 || places > 8) {
            throw new BadRequestException(
                    "Le nombre de places doit etre compris entre 1 et 8"
            );
        }

        if (prixMax != null && prixMax.signum() < 0) {
            throw new BadRequestException(
                    "Le prix maximum doit etre positif ou nul"
            );
        }

        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException(
                    "La page doit etre positive ou nulle et la taille entre 1 et 100"
            );
        }

        PageRequest pagination = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.asc("dateDepart"),
                        Sort.Order.asc("id")
                )
        );

        return repository.rechercher(
                villeDepart,
                villeArrivee,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay(),
                LocalDateTime.now(),
                places,
                prixMax,
                StatutTrajet.PUBLIE,
                StatutReservation.CONFIRMEE,
                pagination
        );
    }
    @Override
    @Transactional
    public TrajetResponse terminer(Long conducteurId, Long trajetId) {

        Trajet trajet = repository.findDetailById(trajetId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Trajet introuvable : " + trajetId
                        )
                );

        if (!trajet.getConducteur().getId().equals(conducteurId)) {
            throw new ForbiddenException(
                    "Ce trajet n'appartient pas a ce conducteur"
            );
        }

        if (trajet.getStatut() != StatutTrajet.PUBLIE
                && trajet.getStatut() != StatutTrajet.COMPLET) {

            throw new ConflictException(
                    "Seul un trajet publie ou complet peut etre termine"
            );
        }

        if (LocalDateTime.now().isBefore(trajet.getDateDepart())) {
            throw new ConflictException(
                    "Impossible de terminer un trajet avant son depart"
            );
        }

        trajet.setStatut(StatutTrajet.TERMINE);

        log.info(
                "Trajet termine : id={}, conducteurId={}",
                trajetId,
                conducteurId
        );

        return mapper.toResponse(trajet);
    }
}