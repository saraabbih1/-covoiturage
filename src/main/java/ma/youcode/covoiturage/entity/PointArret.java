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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Trajet getTrajet() {
        return trajet;
    }

    public void setTrajet(Trajet trajet) {
        this.trajet = trajet;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public LocalDateTime getHeurePassage() {
        return heurePassage;
    }

    public void setHeurePassage(LocalDateTime heurePassage) {
        this.heurePassage = heurePassage;
    }

    public BigDecimal getPrixDepuisDepart() {
        return prixDepuisDepart;
    }

    public void setPrixDepuisDepart(BigDecimal prixDepuisDepart) {
        this.prixDepuisDepart = prixDepuisDepart;
    }
}