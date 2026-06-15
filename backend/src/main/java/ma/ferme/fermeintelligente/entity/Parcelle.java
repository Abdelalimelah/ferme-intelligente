package ma.ferme.fermeintelligente.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "parcelle")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Parcelle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    private Double surface;

    @Column(name = "type_culture", length = 100)
    private String typeCulture;

    @Column(name = "coordonnees_gps", length = 100)
    private String coordonneesGps;

    private Double latitude;
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ferme_id", nullable = false)
    private Ferme ferme;

    @OneToMany(mappedBy = "parcelle")
    private List<Capteur> capteurs = new ArrayList<>();

    @ManyToMany(mappedBy = "parcellesAssignees")
    private Set<Utilisateur> agriculteurs = new HashSet<>();
}
