package ma.ferme.fermeintelligente.entity;

import jakarta.persistence.*;
import lombok.*;
import ma.ferme.fermeintelligente.enums.Role;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "utilisateur")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Column(length = 20)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "role_enum")
    private Role role;

    @Column(length = 20)
    private String statut;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "must_reset_password")
    private Boolean mustResetPassword;

    @ManyToMany
    @JoinTable(name = "gestionnaire_ferme",
            joinColumns = @JoinColumn(name = "gestionnaire_id"),
            inverseJoinColumns = @JoinColumn(name = "ferme_id"))
    @Builder.Default
    private Set<Ferme> fermesGerees = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "agriculteur_parcelle",
            joinColumns = @JoinColumn(name = "agriculteur_id"),
            inverseJoinColumns = @JoinColumn(name = "parcelle_id"))
    @Builder.Default
    private Set<Parcelle> parcellesAssignees = new HashSet<>();
}
