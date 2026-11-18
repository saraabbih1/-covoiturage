package ma.youcode.covoiturage.dto;

public record ConducteurResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String telephone,
        String numeroPermis
) {
}