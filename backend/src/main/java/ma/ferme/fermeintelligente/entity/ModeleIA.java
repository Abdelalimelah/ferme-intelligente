package ma.ferme.fermeintelligente.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modele_ia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModeleIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 20)
    private String version;

    @Column(name = "type_detection", length = 100)
    private String typeDetection;

    @Column(name = "precision_val")
    private Double precisionVal;

    @OneToMany(mappedBy = "modele")
    private List<ResultatAnalyse> resultats = new ArrayList<>();
}
