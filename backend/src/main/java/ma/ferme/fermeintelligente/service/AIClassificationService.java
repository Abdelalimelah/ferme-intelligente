package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ferme.fermeintelligente.dto.AIAnalysisRequest;
import ma.ferme.fermeintelligente.dto.AIAnalysisResponse;
import ma.ferme.fermeintelligente.dto.ParcelleDetailDTO;
import ma.ferme.fermeintelligente.entity.*;
import ma.ferme.fermeintelligente.enums.NiveauAlerte;
import ma.ferme.fermeintelligente.exception.ResourceNotFoundException;
import ma.ferme.fermeintelligente.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIClassificationService {

    private final ImageParcelleRepository imageParcelleRepository;
    private final ModeleIARepository modeleIARepository;
    private final ResultatAnalyseRepository resultatAnalyseRepository;
    private final AlerteRepository alerteRepository;
    private final ParcelleRepository parcelleRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.service.url:http://localhost:8001}")
    private String aiServiceUrl;

    @Transactional
    public ParcelleDetailDTO.DiseaseResultDTO analyzeImage(Long imageId) {
        ImageParcelle image = imageParcelleRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));

        // Call Python AI microservice
        AIAnalysisRequest request = AIAnalysisRequest.builder()
                .imageId(imageId)
                .imagePath(image.getCheminFichier())
                .parcelleId(image.getParcelle().getId())
                .latitude(image.getParcelle().getLatitude())
                .longitude(image.getParcelle().getLongitude())
                .build();

        AIAnalysisResponse aiResponse;
        try {
            ResponseEntity<AIAnalysisResponse> response = restTemplate.postForEntity(
                    aiServiceUrl + "/analyze", request, AIAnalysisResponse.class);
            aiResponse = response.getBody();
        } catch (Exception e) {
            log.error("AI service call failed: {}", e.getMessage());
            // Fallback: generate simulated result for demo
            aiResponse = simulateAIResponse();
        }

        // Get or create AI model reference
        ModeleIA modele = modeleIARepository.findAll().stream().findFirst()
                .orElseGet(() -> modeleIARepository.save(ModeleIA.builder()
                        .nom("PlantDiseaseNet")
                        .version("1.0")
                        .typeDetection("Classification maladies végétales")
                        .precisionVal(0.92)
                        .build()));

        // Update existing result or create new one
        ResultatAnalyse existingResult = image.getResultatAnalyse();
        if (existingResult != null) {
            existingResult.setMaladieDetectee(aiResponse.isHealthy() ? "Healthy" : aiResponse.getDisease());
            existingResult.setNiveauConfiance(aiResponse.getConfidence());
            existingResult.setDateAnalyse(LocalDateTime.now());
            existingResult.setRecommandation(aiResponse.getRecommendation());
            existingResult.setModele(modele);
            resultatAnalyseRepository.save(existingResult);

            return ParcelleDetailDTO.DiseaseResultDTO.builder()
                    .id(existingResult.getId())
                    .maladieDetectee(existingResult.getMaladieDetectee())
                    .niveauConfiance(existingResult.getNiveauConfiance())
                    .dateAnalyse(existingResult.getDateAnalyse())
                    .recommandation(existingResult.getRecommandation())
                    .imagePath(image.getCheminFichier())
                    .build();
        }

        // Store new result
        ResultatAnalyse resultat = ResultatAnalyse.builder()
                .maladieDetectee(aiResponse.isHealthy() ? "Healthy" : aiResponse.getDisease())
                .niveauConfiance(aiResponse.getConfidence())
                .dateAnalyse(LocalDateTime.now())
                .recommandation(aiResponse.getRecommendation())
                .image(image)
                .modele(modele)
                .build();
        resultatAnalyseRepository.save(resultat);

        // Generate alert if disease detected
        if (!aiResponse.isHealthy() && aiResponse.getConfidence() > 0.7) {
            Alerte alerte = Alerte.builder()
                    .type("MALADIE")
                    .message(String.format("Maladie détectée: %s (confiance: %.0f%%) - %s",
                            aiResponse.getDisease(), aiResponse.getConfidence() * 100, aiResponse.getRecommendation()))
                    .niveau(aiResponse.getConfidence() > 0.9 ? NiveauAlerte.CRITIQUE : NiveauAlerte.WARNING)
                    .dateCreation(LocalDateTime.now())
                    .estLue(false)
                    .parcelle(image.getParcelle())
                    .resultat(resultat)
                    .build();
            alerteRepository.save(alerte);
        }

        return ParcelleDetailDTO.DiseaseResultDTO.builder()
                .id(resultat.getId())
                .maladieDetectee(resultat.getMaladieDetectee())
                .niveauConfiance(resultat.getNiveauConfiance())
                .dateAnalyse(resultat.getDateAnalyse())
                .recommandation(resultat.getRecommandation())
                .imagePath(image.getCheminFichier())
                .build();
    }

    public List<ParcelleDetailDTO.DiseaseResultDTO> getDiseasesByParcelle(Long parcelleId) {
        return resultatAnalyseRepository.findByParcelleIdOrderByDateDesc(parcelleId).stream()
                .map(r -> ParcelleDetailDTO.DiseaseResultDTO.builder()
                        .id(r.getId())
                        .maladieDetectee(r.getMaladieDetectee())
                        .niveauConfiance(r.getNiveauConfiance())
                        .dateAnalyse(r.getDateAnalyse())
                        .recommandation(r.getRecommandation())
                        .imagePath(r.getImage().getCheminFichier())
                        .build())
                .toList();
    }

    private AIAnalysisResponse simulateAIResponse() {
        // Simulated response for demo when AI service is unavailable
        String[] diseases = {"Mildiou", "Oïdium", "Rouille", "Tache foliaire", "Healthy"};
        String[] recommendations = {
                "Appliquer un fongicide à base de cuivre",
                "Traitement au soufre recommandé",
                "Retirer les feuilles infectées, appliquer un traitement préventif",
                "Améliorer la ventilation, réduire l'humidité",
                "Aucune action requise"
        };
        int idx = (int) (Math.random() * 5);
        boolean healthy = idx == 4;
        return AIAnalysisResponse.builder()
                .disease(diseases[idx])
                .confidence(healthy ? 0.95 : 0.75 + Math.random() * 0.2)
                .recommendation(recommendations[idx])
                .healthy(healthy)
                .build();
    }
}
