package ma.ferme.fermeintelligente.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AIAnalysisRequest {
    private Long imageId;
    private String imagePath;
    private Long parcelleId;
    private Double latitude;
    private Double longitude;
}
