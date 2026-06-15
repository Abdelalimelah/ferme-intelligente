package ma.ferme.fermeintelligente.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.ferme.fermeintelligente.enums.NiveauAlerte;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AlerteDTO {
    private Long id;
    private String type;
    private String message;
    private NiveauAlerte niveau;
    private LocalDateTime dateCreation;
    private Boolean estLue;
    private Long parcelleId;
    private String parcelleNom;
    private Long resultatId;
}
