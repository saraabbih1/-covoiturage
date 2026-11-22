package ma.youcode.covoiturage.repository;

import ma.youcode.covoiturage.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ma.youcode.covoiturage.dto.TrajetRechercheResponse;
import ma.youcode.covoiturage.enums.StatutReservation;
import ma.youcode.covoiturage.enums.StatutTrajet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    boolean existsByVehicule_Id(Long vehiculeId);

    @Query("""
            SELECT DISTINCT t
            FROM Trajet t
            JOIN FETCH t.conducteur
            JOIN FETCH t.vehicule
            LEFT JOIN FETCH t.pointsArret
            WHERE t.id = :id
            """)
    Optional<Trajet> findDetailById(@Param("id") Long id);
    @Query(
            value = """
                SELECT new ma.youcode.covoiturage.dto.TrajetRechercheResponse(
                    t.id,
                    montee.ville,
                    descente.ville,
                    montee.heurePassage,
                    descente.heurePassage,
                    descente.prixDepuisDepart - montee.prixDepuisDepart
                )
                FROM Trajet t
                JOIN t.pointsArret montee
                JOIN t.pointsArret descente
                WHERE t.statut = :statutTrajet
                  AND LOWER(montee.ville) = LOWER(:depart)
                  AND LOWER(descente.ville) = LOWER(:arrivee)
                  AND montee.ordre < descente.ordre
                  AND montee.heurePassage >= :debut
                  AND montee.heurePassage < :fin
                  AND t.dateDepart > :maintenant
                  AND (
                      :prixMax IS NULL
                      OR descente.prixDepuisDepart
                         - montee.prixDepuisDepart <= :prixMax
                  )
                  AND t.placesProposees - (
                      SELECT COALESCE(SUM(r.nombrePlaces), 0)
                      FROM Reservation r
                      WHERE r.trajet = t
                        AND r.statut = :statutReservation
                  ) >= :places
                """,
            countQuery = """
                SELECT COUNT(t)
                FROM Trajet t
                JOIN t.pointsArret montee
                JOIN t.pointsArret descente
                WHERE t.statut = :statutTrajet
                  AND LOWER(montee.ville) = LOWER(:depart)
                  AND LOWER(descente.ville) = LOWER(:arrivee)
                  AND montee.ordre < descente.ordre
                  AND montee.heurePassage >= :debut
                  AND montee.heurePassage < :fin
                  AND t.dateDepart > :maintenant
                  AND (
                      :prixMax IS NULL
                      OR descente.prixDepuisDepart
                         - montee.prixDepuisDepart <= :prixMax
                  )
                  AND t.placesProposees - (
                      SELECT COALESCE(SUM(r.nombrePlaces), 0)
                      FROM Reservation r
                      WHERE r.trajet = t
                        AND r.statut = :statutReservation
                  ) >= :places
                """
    )
    Page<TrajetRechercheResponse> rechercher(
            @Param("depart") String depart,
            @Param("arrivee") String arrivee,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin,
            @Param("maintenant") LocalDateTime maintenant,
            @Param("places") Integer places,
            @Param("prixMax") BigDecimal prixMax,
            @Param("statutTrajet") StatutTrajet statutTrajet,
            @Param("statutReservation") StatutReservation statutReservation,
            Pageable pageable
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t
        FROM Trajet t
        WHERE t.id = :id
        """)
    Optional<Trajet> findByIdForUpdate(@Param("id") Long id);
}