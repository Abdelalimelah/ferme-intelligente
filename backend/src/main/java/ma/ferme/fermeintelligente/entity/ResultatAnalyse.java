package ma.ferme.fermeintelligente.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resultat_analyse")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResultatAnalyse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "maladie_detectee", length = 150)
    private String maladieDetectee;

    @Column(name = "maladie_fr", length = 150)
    private String maladieFr;

    @Column(name = "class_name", length = 150)
    private String className;

    @Column(name = "niveau_confiance")
    private Double niveauConfiance;

    @Column(name = "date_analyse")
    private LocalDateTime dateAnalyse;

    @Column(columnDefinition = "text")
    private String recommandation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private ImageParcelle image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modele_id", nullable = false)
    private ModeleIA modele;

    @OneToOne(mappedBy = "resultat")
    private Alerte alerte;
}
