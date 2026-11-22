package ma.youcode.covoiturage.repository;

import ma.youcode.covoiturage.entity.Reservation;
import ma.youcode.covoiturage.enums.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    boolean existsByPassager_IdAndTrajet_IdAndStatut(
            Long passagerId,
            Long trajetId,
            StatutReservation statut
    );

    @Query("""
            SELECT COALESCE(SUM(r.nombrePlaces), 0)
            FROM Reservation r
            WHERE r.trajet.id = :trajetId
              AND r.statut = :statut
            """)
    Long compterPlacesReservees(
            @Param("trajetId") Long trajetId,
            @Param("statut") StatutReservation statut
    );
    List<Reservation> findByTrajet_IdAndStatut(
            Long trajetId,
            StatutReservation statut
    );
    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.passager
        JOIN FETCH r.trajet
        JOIN FETCH r.pointMontee
        JOIN FETCH r.pointDescente
        WHERE r.passager.id = :passagerId
        ORDER BY r.dateReservation DESC, r.id DESC
        """)
    List<Reservation> findHistoriqueByPassagerId(
            @Param("passagerId") Long passagerId
    );
}