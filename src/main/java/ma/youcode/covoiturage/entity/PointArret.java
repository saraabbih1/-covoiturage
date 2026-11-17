package ma.youcode.covoiturage.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "points_arret",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"trajet_id", "ordre"}),
                @UniqueConstraint(columnNames = {"trajet_id", "ville"})
        }
)
public class PointArret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trajet_id", nullable = false)
    private Trajet trajet;

    @Column(nullable = false)
    private String ville;

    @Column(nullable = false)
    private Integer ordre;

    @Column(nullable = false)
    private LocalDateTime heurePassage;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prixDepuisDepart;

    public PointArret() {
    }
    public void setTrajet(Trajet trajet) {
        this.trajet = trajet;
    }
}