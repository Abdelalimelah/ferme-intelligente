package ma.ferme.fermeintelligente.entity;

import jakarta.persistence.*;
import lombok.*;
import ma.ferme.fermeintelligente.enums.NiveauAlerte;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerte")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Alerte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "niveau_alerte_enum")
    private NiveauAlerte niveau;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "est_lue")
    private Boolean estLue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcelle_id", nullable = false)
    private Parcelle parcelle;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resultat_id")
    private ResultatAnalyse resultat;
}
