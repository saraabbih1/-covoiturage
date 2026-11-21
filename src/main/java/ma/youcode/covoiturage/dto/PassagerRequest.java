package ma.youcode.covoiturage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PassagerRequest(

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "Le prenom est obligatoire")
        String prenom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email est invalide")
        String email,

        @NotBlank(message = "Le telephone est obligatoire")
        String telephone
) {
}