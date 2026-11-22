package ma.youcode.covoiturage.controller;

import jakarta.validation.Valid;
import ma.youcode.covoiturage.dto.VehiculeRequest;
import ma.youcode.covoiturage.dto.VehiculeResponse;
import ma.youcode.covoiturage.service.VehiculeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conducteurs/{conducteurId}/vehicules")
public class VehiculeController {

    private final VehiculeService service;

    public VehiculeController(VehiculeService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculeResponse creer(
            @PathVariable("conducteurId") Long conducteurId,
            @Valid @RequestBody VehiculeRequest request
    ) {
        return service.creer(conducteurId, request);
    }

    @GetMapping
    public List<VehiculeResponse> lister(
            @PathVariable("conducteurId") Long conducteurId
    ) {
        return service.lister(conducteurId);
    }

    @GetMapping("/{vehiculeId}")
    public VehiculeResponse consulter(
            @PathVariable("conducteurId") Long conducteurId,
            @PathVariable("vehiculeId") Long vehiculeId
    ) {
        return service.consulter(conducteurId, vehiculeId);
    }

    @PutMapping("/{vehiculeId}")
    public VehiculeResponse modifier(
            @PathVariable("conducteurId") Long conducteurId,
            @PathVariable("vehiculeId") Long vehiculeId,
            @Valid @RequestBody VehiculeRequest request
    ) {
        return service.modifier(conducteurId, vehiculeId, request);
    }

    @DeleteMapping("/{vehiculeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(
            @PathVariable("conducteurId") Long conducteurId,
            @PathVariable("vehiculeId") Long vehiculeId
    ) {
        service.supprimer(conducteurId, vehiculeId);
    }
}