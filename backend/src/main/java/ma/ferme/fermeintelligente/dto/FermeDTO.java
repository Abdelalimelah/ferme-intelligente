package ma.ferme.fermeintelligente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FermeDTO {
    private Long id;

    @NotBlank(message = "Le nom de la ferme est requis")
    @Size(max = 150, message = "Le nom ne peut dépasser 150 caractères")
    private String nom;
    private String localisation;
    private Double surface;
    private LocalDateTime dateCreation;
    private Long proprietaireId;
    private String proprietaireNom;
    private int nombreParcelles;
    private int nombreGestionnaires;
}
