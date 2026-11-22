package ma.youcode.covoiturage.controller;

import jakarta.validation.Valid;
import ma.youcode.covoiturage.dto.ReservationRequest;
import ma.youcode.covoiturage.dto.ReservationResponse;
import ma.youcode.covoiturage.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/passagers/{passagerId}/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse creer(
            @PathVariable("passagerId") Long passagerId,
            @Valid @RequestBody ReservationRequest request
    ) {
        return service.creer(passagerId, request);
    }
    @PatchMapping("/{reservationId}/annuler")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void annuler(
            @PathVariable("passagerId") Long passagerId,
            @PathVariable("reservationId") Long reservationId
    ) {
        service.annuler(passagerId, reservationId);
    }
    @GetMapping
    public List<ReservationResponse> historique(
            @PathVariable("passagerId") Long passagerId
    ) {
        return service.historique(passagerId);
    }
}