package ma.ferme.fermeintelligente.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "image_parcelle")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ImageParcelle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chemin_fichier", nullable = false, length = 500)
    private String cheminFichier;

    @Column(name = "date_capture", nullable = false)
    private LocalDateTime dateCapture;

    @Column(length = 20)
    private String resolution;

    @Column(columnDefinition = "text")
    private String metadonnees;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcelle_id", nullable = false)
    private Parcelle parcelle;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drone_id")
    private Drone drone;

    @OneToOne(mappedBy = "image", cascade = CascadeType.ALL)
    private ResultatAnalyse resultatAnalyse;
}
