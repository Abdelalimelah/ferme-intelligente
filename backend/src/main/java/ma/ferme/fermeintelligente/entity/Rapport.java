package ma.ferme.fermeintelligente.entity;

import jakarta.persistence.*;
import lombok.*;
import ma.ferme.fermeintelligente.enums.StatutRapport;
import ma.ferme.fermeintelligente.enums.TypeRapport;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "rapport")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rapport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "type_rapport_enum")
    private TypeRapport type;

    @Column(nullable = false, length = 255)
    private String sujet;

    @Column(nullable = false, columnDefinition = "text")
    private String contenu;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "statut_rapport_enum")
    private StatutRapport statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auteur_id", nullable = false)
    private Utilisateur auteur;
}
