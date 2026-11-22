package ma.youcode.covoiturage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record TrajetRequest(

        @NotNull(message = "Le vehicule est obligatoire")
        @Positive(message = "L'identifiant du vehicule doit etre positif")
        Long vehiculeId,

        @NotNull(message = "Le nombre de places est obligatoire")
        @Min(value = 1, message = "Il faut proposer au moins une place")
        Integer placesProposees,

        @NotNull(message = "Les points d'arret sont obligatoires")
        @Size(min = 2, message = "Il faut au moins deux points d'arret")
        List<@NotNull @Valid PointArretRequest> pointsArret
) {
}