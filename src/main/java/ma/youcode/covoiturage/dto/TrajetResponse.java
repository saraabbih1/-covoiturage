package ma.youcode.covoiturage.dto;

import ma.youcode.covoiturage.enums.StatutTrajet;

import java.time.LocalDateTime;
import java.util.List;

public record TrajetResponse(
        Long id,
        Long conducteurId,
        Long vehiculeId,
        String villeDepart,
        String villeArrivee,
        LocalDateTime dateDepart,
        Integer placesProposees,
        StatutTrajet statut,
        List<PointArretResponse> pointsArret
) {
}