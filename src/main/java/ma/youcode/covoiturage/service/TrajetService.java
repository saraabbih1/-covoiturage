package ma.youcode.covoiturage.service;
import ma.youcode.covoiturage.dto.TrajetRechercheResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import ma.youcode.covoiturage.dto.TrajetRequest;
import ma.youcode.covoiturage.dto.TrajetResponse;

public interface TrajetService {
    TrajetResponse terminer(Long conducteurId, Long trajetId);
    TrajetResponse consulter(Long id);
    TrajetResponse publier(Long conducteurId, TrajetRequest request);
    Page<TrajetRechercheResponse> rechercher(
            String depart,
            String arrivee,
            LocalDate date,
            Integer places,
            BigDecimal prixMax,
            int page,
            int size
    );
    void annuler(Long conducteurId, Long trajetId);
}