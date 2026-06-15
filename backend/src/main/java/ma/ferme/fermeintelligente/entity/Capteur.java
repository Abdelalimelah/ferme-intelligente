package ma.ferme.fermeintelligente.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "capteur")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Capteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(length = 20)
    private String unite;

    @Column(length = 20)
    private String statut;

    @Column(name = "date_installation")
    private LocalDate dateInstallation;

    @Column(name = "valeur_min")
    private Double valeurMin;

    @Column(name = "valeur_max")
    private Double valeurMax;

    @Column(name = "derniere_valeur")
    private Double derniereValeur;

    @Column(name = "derniere_lecture")
    private LocalDateTime derniereLecture;

    @Column(name = "api_key", length = 64)
    private String apiKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcelle_id", nullable = false)
    private Parcelle parcelle;

    @OneToMany(mappedBy = "capteur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DonneeCapteur> donneeCapteurs = new ArrayList<>();
}
