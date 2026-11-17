package ma.youcode.covoiturage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "passagers")
public class Passager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telephone;

    public Passager() {
    }
}