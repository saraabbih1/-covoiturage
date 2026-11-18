package ma.youcode.covoiturage.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ConducteurRequest(

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        String prenom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email est invalide")
        String email,

        @NotBlank(message = "Le téléphone est obligatoire")
        String telephone,

        @NotBlank(message = "Le numéro de permis est obligatoire")
        String numeroPermis

) {
}