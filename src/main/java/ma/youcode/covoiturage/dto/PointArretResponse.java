package ma.youcode.covoiturage.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PointArretResponse(
        Long id,
        String ville,
        Integer ordre,
        LocalDateTime heurePassage,
        BigDecimal prixDepuisDepart
) {
}