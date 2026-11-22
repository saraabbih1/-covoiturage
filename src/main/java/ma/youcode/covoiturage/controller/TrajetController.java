package ma.youcode.covoiturage.controller;

import jakarta.validation.Valid;
import ma.youcode.covoiturage.dto.TrajetRequest;
import ma.youcode.covoiturage.dto.TrajetResponse;
import ma.youcode.covoiturage.service.TrajetService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conducteurs/{conducteurId}/trajets")
public class TrajetController {

    private final TrajetService service;

    public TrajetController(TrajetService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrajetResponse publier(
            @PathVariable("conducteurId") Long conducteurId,
            @Valid @RequestBody TrajetRequest request
    ) {
        return service.publier(conducteurId, request);
    }
    @PatchMapping("/{trajetId}/terminer")
    public TrajetResponse terminer(
            @PathVariable("conducteurId") Long conducteurId,
            @PathVariable("trajetId") Long trajetId
    ) {
        return service.terminer(conducteurId, trajetId);
    }
    @PatchMapping("/{trajetId}/annuler")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void annuler(
            @PathVariable("conducteurId") Long conducteurId,
            @PathVariable("trajetId") Long trajetId
    ) {
        service.annuler(conducteurId, trajetId);
    }
}