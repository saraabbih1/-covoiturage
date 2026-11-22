package ma.youcode.covoiturage.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointArretRequest(

        @NotBlank(message = "La ville est obligatoire")
        String ville,

        @NotNull(message = "L'ordre est obligatoire")
        @Min(value = 1, message = "L'ordre doit commencer a 1")
        Integer ordre,

        @NotNull(message = "L'heure de passage est obligatoire")
        LocalDateTime heurePassage,

        @NotNull(message = "Le prix est obligatoire")
        @DecimalMin(value = "0.00", message = "Le prix doit etre positif ou nul")
        @Digits(integer = 10, fraction = 2,
                message = "Le prix doit avoir au maximum deux decimales")
        BigDecimal prixDepuisDepart
) {
}