package ma.youcode.covoiturage.controller;

import ma.youcode.covoiturage.dto.TrajetResponse;
import ma.youcode.covoiturage.service.TrajetService;
import org.springframework.web.bind.annotation.*;
import ma.youcode.covoiturage.dto.TrajetRechercheResponse;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
@RestController
@RequestMapping("/api/trajets")
public class TrajetConsultationController {

    private final TrajetService service;

    public TrajetConsultationController(TrajetService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public TrajetResponse consulter(
            @PathVariable("id") Long id
    ) {
        return service.consulter(id);
    }
    @GetMapping("/recherche")
    public Page<TrajetRechercheResponse> rechercher(

            @RequestParam(name = "depart", defaultValue = "")
            String depart,

            @RequestParam(name = "arrivee", defaultValue = "")
            String arrivee,

            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(name = "places", defaultValue = "1")
            Integer places,

            @RequestParam(name = "prixMax", required = false)
            BigDecimal prixMax,

            @RequestParam(name = "page", defaultValue = "0")
            int page,

            @RequestParam(name = "size", defaultValue = "10")
            int size
    ) {
        return service.rechercher(
                depart,
                arrivee,
                date,
                places,
                prixMax,
                page,
                size
        );
    }
}