package ma.youcode.covoiturage.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TrajetRechercheResponse(
        Long id,
        String villeMontee,
        String villeDescente,
        LocalDateTime heureMontee,
        LocalDateTime heureDescente,
        BigDecimal prixParPlace
) {
}