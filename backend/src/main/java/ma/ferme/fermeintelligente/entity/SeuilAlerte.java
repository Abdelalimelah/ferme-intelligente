package ma.ferme.fermeintelligente.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "seuil_alerte", uniqueConstraints = @UniqueConstraint(columnNames = {"type_capteur", "parcelle_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeuilAlerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_capteur", nullable = false, length = 50)
    private String typeCapteur;

    @Column(name = "valeur_min", nullable = false)
    private Double valeurMin;

    @Column(name = "valeur_max", nullable = false)
    private Double valeurMax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcelle_id", nullable = false)
    private Parcelle parcelle;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
}
