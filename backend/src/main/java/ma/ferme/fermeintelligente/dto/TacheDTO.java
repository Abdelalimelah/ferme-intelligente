package ma.ferme.fermeintelligente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.ferme.fermeintelligente.enums.Priorite;
import ma.ferme.fermeintelligente.enums.StatutTache;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TacheDTO {
    private Long id;

    @NotBlank(message = "Le titre est requis")
    @Size(max = 200, message = "Le titre ne peut dépasser 200 caractères")
    private String titre;

    private String description;
    private StatutTache statut;
    private Priorite priorite;
    private LocalDateTime dateCreation;
    private LocalDateTime dateEcheance;
    private LocalDateTime dateTerminee;

    @NotNull(message = "L'agriculteur est requis")
    private Long agriculteurId;
    private String agriculteurNom;

    @NotNull(message = "Le gestionnaire est requis")
    private Long gestionnaireId;
    private String gestionnaireNom;

    private Long parcelleId;
    private String parcelleNom;
}
