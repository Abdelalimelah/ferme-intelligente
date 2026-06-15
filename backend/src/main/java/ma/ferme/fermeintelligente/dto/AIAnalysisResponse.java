package ma.ferme.fermeintelligente.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AIAnalysisResponse {
    private String disease;
    private Double confidence;
    private String recommendation;
    private boolean healthy;
}
