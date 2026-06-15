package ma.ferme.fermeintelligente.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "drone")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Drone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String modele;

    @Column(length = 20)
    private String statut;

    private Double autonomie;

    @OneToMany(mappedBy = "drone")
    private List<ImageParcelle> images = new ArrayList<>();
}
