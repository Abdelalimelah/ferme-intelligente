package ma.ferme.fermeintelligente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.ferme.fermeintelligente.enums.StatutRapport;
import ma.ferme.fermeintelligente.enums.TypeRapport;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RapportDTO {
    private Long id;

    @NotNull(message = "Le type de rapport est requis")
    private TypeRapport type;

    @NotBlank(message = "Le sujet est requis")
    @Size(max = 255, message = "Le sujet ne peut dépasser 255 caractères")
    private String sujet;

    @NotBlank(message = "Le contenu est requis")
    private String contenu;

    private LocalDateTime dateCreation;
    private StatutRapport statut;

    @NotNull(message = "L'auteur est requis")
    private Long auteurId;
    private String auteurNom;
}
