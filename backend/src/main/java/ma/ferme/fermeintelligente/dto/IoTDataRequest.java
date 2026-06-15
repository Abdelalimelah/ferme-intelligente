package ma.ferme.fermeintelligente.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class IoTDataRequest {
    private Long capteurId;
    private String apiKey;
    private Double valeur;
    private String unite;
    private String type; // Température, Humidité, pH
    private Long parcelleId; // alternative to capteurId: identify by parcelle + type
}
