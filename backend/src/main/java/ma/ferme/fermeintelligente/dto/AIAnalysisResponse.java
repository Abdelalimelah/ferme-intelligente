package ma.ferme.fermeintelligente.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AIAnalysisResponse {
    private String disease;
    private String disease_fr;
    private String class_name;
    private Double confidence;
    private String recommendation;
    private boolean healthy;
    private String imageUrl;
    private String imageSavedPath;
}
