package ma.youcode.covoiturage.controller;

import jakarta.validation.Valid;
import ma.youcode.covoiturage.dto.PassagerRequest;
import ma.youcode.covoiturage.dto.PassagerResponse;
import ma.youcode.covoiturage.service.PassagerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passagers")
public class PassagerController {

    private final PassagerService service;

    public PassagerController(PassagerService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PassagerResponse creer(
            @Valid @RequestBody PassagerRequest request
    ) {
        return service.creer(request);
    }

    @GetMapping
    public List<PassagerResponse> lister() {
        return service.lister();
    }

    @GetMapping("/{id}")
    public PassagerResponse consulter(
            @PathVariable("id") Long id
    ) {
        return service.consulter(id);
    }

    @PutMapping("/{id}")
    public PassagerResponse modifier(
            @PathVariable("id") Long id,
            @Valid @RequestBody PassagerRequest request
    ) {
        return service.modifier(id, request);
    }

    @GetMapping("/recherche")
    public List<PassagerResponse> rechercher(
            @RequestParam(name = "email", defaultValue = "")
            String email
    ) {
        return service.rechercher(email);
    }
}