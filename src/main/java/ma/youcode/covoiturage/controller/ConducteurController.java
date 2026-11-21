package ma.youcode.covoiturage.controller;

import jakarta.validation.Valid;
import ma.youcode.covoiturage.dto.ConducteurRequest;
import ma.youcode.covoiturage.dto.ConducteurResponse;
import ma.youcode.covoiturage.service.ConducteurService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conducteurs")
public class ConducteurController {

    private final ConducteurService service;

    public ConducteurController(ConducteurService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConducteurResponse creer(
            @Valid @RequestBody ConducteurRequest request
    ) {
        return service.creer(request);
    }

    @GetMapping
    public List<ConducteurResponse> lister() {
        return service.lister();
    }

    @GetMapping("/{id}")
    public ConducteurResponse consulter(
            @PathVariable("id") Long id
    ) {
        return service.consulter(id);
    }

    @PutMapping("/{id}")
    public ConducteurResponse modifier(
            @PathVariable("id") Long id,
            @Valid @RequestBody ConducteurRequest request
    ) {
        return service.modifier(id, request);
    }

    @GetMapping("/recherche")
    public List<ConducteurResponse> rechercher(
            @RequestParam(name = "recherche", defaultValue = "")
            String recherche
    ) {
        return service.rechercher(recherche);
    }
}