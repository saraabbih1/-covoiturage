package ma.youcode.covoiturage.service;

import ma.youcode.covoiturage.dto.ConducteurRequest;
import ma.youcode.covoiturage.dto.ConducteurResponse;
import ma.youcode.covoiturage.entity.Conducteur;
import ma.youcode.covoiturage.exception.ConflictException;
import ma.youcode.covoiturage.exception.ResourceNotFoundException;
import ma.youcode.covoiturage.mapper.ConducteurMapper;
import ma.youcode.covoiturage.repository.ConducteurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConducteurServiceImpl implements ConducteurService {

    private static final Logger log =
            LoggerFactory.getLogger(ConducteurServiceImpl.class);

    private final ConducteurRepository repository;
    private final ConducteurMapper mapper;

    public ConducteurServiceImpl(
            ConducteurRepository repository,
            ConducteurMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ConducteurResponse creer(ConducteurRequest request) {

        if (repository.existsByEmail(request.email())) {
            throw new ConflictException("Email deja utilise");
        }

        if (repository.existsByNumeroPermis(request.numeroPermis())) {
            throw new ConflictException("Numero de permis deja utilise");
        }

        Conducteur conducteur = mapper.toEntity(request);
        Conducteur saved = repository.save(conducteur);

        log.info("Conducteur cree : id={}", saved.getId());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConducteurResponse> lister() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConducteurResponse consulter(Long id) {

        Conducteur conducteur = trouverConducteur(id);

        return mapper.toResponse(conducteur);
    }

    @Override
    @Transactional
    public ConducteurResponse modifier(
            Long id,
            ConducteurRequest request
    ) {
        Conducteur conducteur = trouverConducteur(id);

        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictException("Email deja utilise");
        }

        if (repository.existsByNumeroPermisAndIdNot(
                request.numeroPermis(), id)) {

            throw new ConflictException("Numero de permis deja utilise");
        }

        mapper.update(request, conducteur);

        Conducteur saved = repository.save(conducteur);

        log.info("Conducteur modifie : id={}", saved.getId());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConducteurResponse> rechercher(String recherche) {

        String terme = recherche == null ? "" : recherche.trim();

        return repository
                .findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        terme,
                        terme
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    private Conducteur trouverConducteur(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conducteur introuvable : " + id
                        )
                );
    }
}