package ma.youcode.covoiturage.service;

import ma.youcode.covoiturage.dto.PassagerRequest;
import ma.youcode.covoiturage.dto.PassagerResponse;
import ma.youcode.covoiturage.entity.Passager;
import ma.youcode.covoiturage.exception.ResourceNotFoundException;
import ma.youcode.covoiturage.mapper.PassagerMapper;
import ma.youcode.covoiturage.repository.PassagerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PassagerServiceImpl implements PassagerService {

    private static final Logger log =
            LoggerFactory.getLogger(PassagerServiceImpl.class);

    private final PassagerRepository repository;
    private final PassagerMapper mapper;

    public PassagerServiceImpl(
            PassagerRepository repository,
            PassagerMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public PassagerResponse creer(PassagerRequest request) {

        Passager passager = mapper.toEntity(request);
        Passager saved = repository.save(passager);

        log.info("Passager cree : id={}", saved.getId());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassagerResponse> lister() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PassagerResponse consulter(Long id) {
        return mapper.toResponse(trouverPassager(id));
    }

    @Override
    @Transactional
    public PassagerResponse modifier(
            Long id,
            PassagerRequest request
    ) {
        Passager passager = trouverPassager(id);

        mapper.update(request, passager);

        Passager saved = repository.save(passager);

        log.info("Passager modifie : id={}", saved.getId());

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassagerResponse> rechercher(String email) {

        String terme = email == null ? "" : email.trim();

        return repository.findByEmailContainingIgnoreCase(terme)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    private Passager trouverPassager(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Passager introuvable : " + id
                        )
                );
    }
}