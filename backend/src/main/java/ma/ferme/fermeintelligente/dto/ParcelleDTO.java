package ma.ferme.fermeintelligente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ParcelleDTO {
    private Long id;

    @NotBlank(message = "Le nom de la parcelle est requis")
    @Size(max = 100, message = "Le nom ne peut dépasser 100 caractères")
    private String nom;

    @Positive(message = "La surface doit être positive")
    private Double surface;

    private String typeCulture;
    private String coordonneesGps;

    @NotNull(message = "La ferme est requise")
    private Long fermeId;

    private String fermeNom;
    private int nombreCapteurs;
    private int nombreAgriculteurs;
}
