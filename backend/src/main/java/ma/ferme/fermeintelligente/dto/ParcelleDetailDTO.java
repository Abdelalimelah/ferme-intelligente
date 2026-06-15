package ma.ferme.fermeintelligente.dto;

import lombok.*;
import ma.ferme.fermeintelligente.enums.NiveauAlerte;

import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ParcelleDetailDTO {
    private Long id;
    private String nom;
    private Double surface;
    private String typeCulture;
    private String coordonneesGps;
    private Double latitude;
    private Double longitude;
    private String fermeNom;

    private List<CapteurLiveDTO> capteurs;
    private List<AlerteDTO> alertesRecentes;
    private List<DiseaseResultDTO> maladiesDetectees;
    private List<ImageDTO> imagesRecentes;
    private SensorStatsDTO stats;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CapteurLiveDTO {
        private Long id;
        private String type;
        private String unite;
        private String statut;
        private Double derniereValeur;
        private LocalDateTime derniereLecture;
        private Double valeurMin;
        private Double valeurMax;
        private boolean enAlerte;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DiseaseResultDTO {
        private Long id;
        private String maladieDetectee;
        private Double niveauConfiance;
        private LocalDateTime dateAnalyse;
        private String recommandation;
        private String imagePath;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ImageDTO {
        private Long id;
        private String cheminFichier;
        private LocalDateTime dateCapture;
        private String resolution;
        private boolean analysee;
        private String maladieDetectee;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SensorStatsDTO {
        private Double tempMoyenne;
        private Double humiditeMoyenne;
        private Double phMoyen;
        private int totalAlertes;
        private int alertesCritiques;
        private int maladiesDetectees;
    }
}
