package ma.ferme.fermeintelligente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CapteurDTO {
    private Long id;

    @NotBlank(message = "Le type de capteur est requis")
    @Size(max = 50, message = "Le type ne peut dépasser 50 caractères")
    private String type;

    @Size(max = 20, message = "L'unité ne peut dépasser 20 caractères")
    private String unite;

    private String statut;
    private LocalDate dateInstallation;

    @NotNull(message = "La parcelle est requise")
    private Long parcelleId;

    private String parcelleNom;
    private Double derniereValeur;
    private LocalDateTime derniereLecture;
}
