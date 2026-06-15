package ma.ferme.fermeintelligente.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "donnee_capteur")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DonneeCapteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double valeur;

    @Column(length = 20)
    private String unite;

    @Column(name = "date_releve", nullable = false)
    private LocalDateTime dateReleve;

    @PrePersist
    public void prePersist() {
        this.dateReleve = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capteur_id", nullable = false)
    private Capteur capteur;
}
