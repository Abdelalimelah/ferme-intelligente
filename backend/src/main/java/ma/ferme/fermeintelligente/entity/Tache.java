package ma.ferme.fermeintelligente.entity;

import jakarta.persistence.*;
import lombok.*;
import ma.ferme.fermeintelligente.enums.Priorite;
import ma.ferme.fermeintelligente.enums.StatutTache;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "tache")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "statut_tache_enum")
    private StatutTache statut;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "priorite_tache_enum")
    private Priorite priorite;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_echeance")
    private LocalDateTime dateEcheance;

    @Column(name = "date_terminee")
    private LocalDateTime dateTerminee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agriculteur_id", nullable = false)
    private Utilisateur agriculteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestionnaire_id", nullable = false)
    private Utilisateur gestionnaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcelle_id")
    private Parcelle parcelle;
}
