package ma.youcode.covoiturage.service;

import ma.youcode.covoiturage.dto.VehiculeRequest;
import ma.youcode.covoiturage.dto.VehiculeResponse;
import ma.youcode.covoiturage.entity.Conducteur;
import ma.youcode.covoiturage.entity.Vehicule;
import ma.youcode.covoiturage.exception.ConflictException;
import ma.youcode.covoiturage.exception.ForbiddenException;
import ma.youcode.covoiturage.exception.ResourceNotFoundException;
import ma.youcode.covoiturage.mapper.VehiculeMapper;
import ma.youcode.covoiturage.repository.ConducteurRepository;
import ma.youcode.covoiturage.repository.TrajetRepository;
import ma.youcode.covoiturage.repository.VehiculeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehiculeServiceImpl implements VehiculeService {

    private static final Logger log =
            LoggerFactory.getLogger(VehiculeServiceImpl.class);

    private final VehiculeRepository repository;
    private final ConducteurRepository conducteurRepository;
    private final TrajetRepository trajetRepository;
    private final VehiculeMapper mapper;

    public VehiculeServiceImpl(
            VehiculeRepository repository,
            ConducteurRepository conducteurRepository,
            TrajetRepository trajetRepository,
            VehiculeMapper mapper
    ) {
        this.repository = repository;
        this.conducteurRepository = conducteurRepository;
        this.trajetRepository = trajetRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public VehiculeResponse creer(
            Long conducteurId,
            VehiculeRequest request
    ) {
        Conducteur conducteur = trouverConducteur(conducteurId);

        if (repository.existsByImmatriculation(
                request.immatriculation())) {

            throw new ConflictException(
                    "Immatriculation deja utilisee"
            );
        }

        Vehicule vehicule = mapper.toEntity(request);
        vehicule.setConducteur(conducteur);

        Vehicule saved = repository.save(vehicule);

        log.info(
                "Vehicule cree : id={}, conducteurId={}",
                saved.getId(),
                conducteurId
        );

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponse> lister(Long conducteurId) {

        trouverConducteur(conducteurId);

        return repository.findByConducteur_Id(conducteurId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculeResponse consulter(
            Long conducteurId,
            Long vehiculeId
    ) {
        trouverConducteur(conducteurId);

        Vehicule vehicule = trouverVehicule(vehiculeId);
        verifierProprietaire(vehicule, conducteurId);

        return mapper.toResponse(vehicule);
    }

    @Override
    @Transactional
    public VehiculeResponse modifier(
            Long conducteurId,
            Long vehiculeId,
            VehiculeRequest request
    ) {
        trouverConducteur(conducteurId);

        Vehicule vehicule = trouverVehicule(vehiculeId);
        verifierProprietaire(vehicule, conducteurId);

        if (repository.existsByImmatriculationAndIdNot(
                request.immatriculation(),
                vehiculeId)) {

            throw new ConflictException(
                    "Immatriculation deja utilisee"
            );
        }

        mapper.update(request, vehicule);

        Vehicule saved = repository.save(vehicule);

        log.info(
                "Vehicule modifie : id={}, conducteurId={}",
                vehiculeId,
                conducteurId
        );

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void supprimer(Long conducteurId, Long vehiculeId) {

        trouverConducteur(conducteurId);

        Vehicule vehicule = trouverVehicule(vehiculeId);
        verifierProprietaire(vehicule, conducteurId);

        if (trajetRepository.existsByVehicule_Id(vehiculeId)) {
            throw new ConflictException(
                    "Impossible de supprimer un vehicule lie a un trajet"
            );
        }

        repository.delete(vehicule);

        log.info(
                "Vehicule supprime : id={}, conducteurId={}",
                vehiculeId,
                conducteurId
        );
    }

    private Conducteur trouverConducteur(Long id) {
        return conducteurRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conducteur introuvable : " + id
                        )
                );
    }

    private Vehicule trouverVehicule(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehicule introuvable : " + id
                        )
                );
    }

    private void verifierProprietaire(
            Vehicule vehicule,
            Long conducteurId
    ) {
        if (!vehicule.getConducteur().getId().equals(conducteurId)) {

            throw new ForbiddenException(
                    "Ce vehicule n'appartient pas a ce conducteur"
            );
        }
    }
}