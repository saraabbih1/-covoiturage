package ma.youcode.covoiturage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehiculeRequest(

        @NotBlank(message = "La marque est obligatoire")
        String marque,

        @NotBlank(message = "Le modele est obligatoire")
        String modele,

        @NotBlank(message = "L'immatriculation est obligatoire")
        String immatriculation,

        @NotBlank(message = "La couleur est obligatoire")
        String couleur,

        @NotNull(message = "La capacite est obligatoire")
        @Min(value = 1, message = "La capacite minimale est 1")
        @Max(value = 8, message = "La capacite maximale est 8")
        Integer capacite
) {
}