package ma.ferme.fermeintelligente.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DonneeCapteurDTO {
    private Long id;
    private Double valeur;
    private String unite;
    private LocalDateTime dateReleve;
    private Long capteurId;
    private String capteurType;
}
