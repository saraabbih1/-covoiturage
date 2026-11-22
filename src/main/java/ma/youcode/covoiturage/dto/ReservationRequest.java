package ma.youcode.covoiturage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationRequest(

        @NotNull(message = "Le trajet est obligatoire")
        @Positive(message = "L'identifiant du trajet doit etre positif")
        Long trajetId,

        @NotNull(message = "Le point de montee est obligatoire")
        @Positive(message = "L'identifiant du point doit etre positif")
        Long pointMonteeId,

        @NotNull(message = "Le point de descente est obligatoire")
        @Positive(message = "L'identifiant du point doit etre positif")
        Long pointDescenteId,

        @NotNull(message = "Le nombre de places est obligatoire")
        @Min(value = 1, message = "Il faut reserver au moins une place")
        Integer nombrePlaces
) {
}