package ma.youcode.covoiturage.entity;

import jakarta.persistence.*;
import ma.youcode.covoiturage.enums.StatutTrajet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trajets")
public class Trajet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conducteur_id", nullable = false)
    private Conducteur conducteur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @Column(nullable = false)
    private String villeDepart;

    @Column(nullable = false)
    private String villeArrivee;

    @Column(nullable = false)
    private LocalDateTime dateDepart;

    @Column(nullable = false)
    private Integer placesProposees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTrajet statut;

    @OneToMany(
            mappedBy = "trajet",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("ordre ASC")
    private List<PointArret> pointsArret = new ArrayList<>();

    public Trajet() {
    }

    public void ajouterPointArret(PointArret point) {
        pointsArret.add(point);
        point.setTrajet(this);
    }
}