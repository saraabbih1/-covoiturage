package ma.youcode.covoiturage.service;
import java.util.List;
import ma.youcode.covoiturage.dto.ReservationRequest;
import ma.youcode.covoiturage.dto.ReservationResponse;

public interface ReservationService {
    void annuler(Long passagerId, Long reservationId);
    ReservationResponse creer(
            Long passagerId,
            ReservationRequest request
    );
    List<ReservationResponse> historique(Long passagerId);
}