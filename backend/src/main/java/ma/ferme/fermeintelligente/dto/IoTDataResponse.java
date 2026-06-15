package ma.ferme.fermeintelligente.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class IoTDataResponse {
    private Long id;
    private Double valeur;
    private String unite;
    private String capteurType;
    private Long capteurId;
    private Long parcelleId;
    private String parcelleNom;
    private boolean alerteGeneree;
    private String alerteMessage;
}
