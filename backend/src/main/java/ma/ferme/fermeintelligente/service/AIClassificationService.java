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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    /**
     * Drone simulation source: pick a random dataset image matching the parcelle's
     * plant type, run inference, and persist the result (used both for manual
     * "dataset" trigger and the automated drone simulation scheduler).
     */
    @Transactional
    public ParcelleDetailDTO.DiseaseResultDTO analyserDepuisDataset(Long parcelleId) {
        return analyserDepuisDataset(parcelleId, null);
    }

    @Transactional
    public ParcelleDetailDTO.DiseaseResultDTO analyserDepuisDataset(Long parcelleId, Drone drone) {
        Parcelle parcelle = parcelleRepository.findById(parcelleId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcelle not found: " + parcelleId));

        java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("plantType", parcelle.getTypeCulture());
        requestBody.put("parcelleId", parcelleId);

        AIAnalysisResponse aiResponse;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            var entity = new HttpEntity<>(requestBody, headers);
            var response = restTemplate.postForEntity(
                    aiServiceUrl + "/analyze/dataset", entity, AIAnalysisResponse.class);
            aiResponse = response.getBody();
            if (aiResponse == null) throw new RuntimeException("Empty response from AI service");
        } catch (Exception e) {
            log.error("AI dataset call failed: {}", e.getMessage());
            aiResponse = simulateAIResponse();
        }

        String source = drone != null ? "drone-simulation" : "dataset-manuel";
        return persistAnalysis(parcelle, aiResponse,
                aiResponse.getImageSavedPath() != null ? aiResponse.getImageSavedPath() : "dataset-simulated",
                source, drone);
    }

    /**
     * Manual analysis source: user uploads their own picture for a chosen parcelle.
     */
    @Transactional
    public ParcelleDetailDTO.DiseaseResultDTO analyserDepuisUpload(Long parcelleId, MultipartFile file) {
        Parcelle parcelle = parcelleRepository.findById(parcelleId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcelle not found: " + parcelleId));

        AIAnalysisResponse aiResponse;
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("file", resource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            var entity = new HttpEntity<>(body, headers);

            var response = restTemplate.postForEntity(
                    aiServiceUrl + "/analyze/upload?parcelle_id=" + parcelleId, entity, AIAnalysisResponse.class);
            aiResponse = response.getBody();
            if (aiResponse == null) throw new RuntimeException("Empty response from AI service");
        } catch (IOException e) {
            throw new RuntimeException("Could not read uploaded file: " + e.getMessage(), e);
        } catch (RestClientException e) {
            log.error("AI upload call failed: {}", e.getMessage());
            aiResponse = simulateAIResponse();
        }

        return persistAnalysis(parcelle, aiResponse,
                aiResponse.getImageUrl() != null ? aiResponse.getImageUrl() : "upload-manuel",
                "upload-manuel", null);
    }

    private ParcelleDetailDTO.DiseaseResultDTO persistAnalysis(
            Parcelle parcelle, AIAnalysisResponse aiResponse, String cheminFichier, String source, Drone drone) {

        ModeleIA modele = modeleIARepository.findAll().stream().findFirst()
                .orElseGet(() -> modeleIARepository.save(ModeleIA.builder()
                        .nom("ResNet9-PlantVillage")
                        .version("1.0")
                        .typeDetection("Classification maladies végétales")
                        .precisionVal(0.92)
                        .build()));

        ImageParcelle image = ImageParcelle.builder()
                .cheminFichier(cheminFichier)
                .imageUrl(aiResponse.getImageUrl())
                .dateCapture(LocalDateTime.now())
                .metadonnees("source:" + source + ";plant:" + parcelle.getTypeCulture())
                .parcelle(parcelle)
                .drone(drone)
                .build();
        imageParcelleRepository.save(image);

        ResultatAnalyse resultat = ResultatAnalyse.builder()
                .maladieDetectee(aiResponse.getDisease())
                .maladieFr(aiResponse.getDisease_fr())
                .className(aiResponse.getClass_name())
                .niveauConfiance(aiResponse.getConfidence())
                .dateAnalyse(LocalDateTime.now())
                .recommandation(aiResponse.getRecommendation())
                .image(image)
                .modele(modele)
                .build();
        resultatAnalyseRepository.save(resultat);

        if (!aiResponse.isHealthy() && aiResponse.getConfidence() > 0.7) {
            Alerte alerte = Alerte.builder()
                    .type("MALADIE")
                    .message(String.format("Maladie détectée sur %s: %s (confiance: %.0f%%) — %s",
                            parcelle.getNom(),
                            aiResponse.getDisease_fr() != null ? aiResponse.getDisease_fr() : aiResponse.getDisease(),
                            aiResponse.getConfidence() * 100,
                            aiResponse.getRecommendation()))
                    .niveau(aiResponse.getConfidence() > 0.9 ? NiveauAlerte.CRITIQUE : NiveauAlerte.WARNING)
                    .dateCreation(LocalDateTime.now())
                    .estLue(false)
                    .parcelle(parcelle)
                    .resultat(resultat)
                    .build();
            alerteRepository.save(alerte);
        }

        return ParcelleDetailDTO.DiseaseResultDTO.builder()
                .id(resultat.getId())
                .maladieDetectee(resultat.getMaladieDetectee())
                .maladieFr(resultat.getMaladieFr())
                .className(resultat.getClassName())
                .niveauConfiance(resultat.getNiveauConfiance())
                .dateAnalyse(resultat.getDateAnalyse())
                .recommandation(resultat.getRecommandation())
                .imagePath(image.getCheminFichier())
                .imageUrl(image.getImageUrl())
                .sain(aiResponse.isHealthy())
                .parcelleNom(parcelle.getNom())
                .typeCulture(parcelle.getTypeCulture())
                .build();
    }

    public List<ParcelleDetailDTO.DiseaseResultDTO> getDiseasesByParcelle(Long parcelleId) {
        return resultatAnalyseRepository.findByParcelleIdOrderByDateDesc(parcelleId).stream()
                .map(r -> ParcelleDetailDTO.DiseaseResultDTO.builder()
                        .id(r.getId())
                        .maladieDetectee(r.getMaladieDetectee())
                        .maladieFr(r.getMaladieFr())
                        .className(r.getClassName())
                        .niveauConfiance(r.getNiveauConfiance())
                        .dateAnalyse(r.getDateAnalyse())
                        .recommandation(r.getRecommandation())
                        .imagePath(r.getImage().getCheminFichier())
                        .imageUrl(r.getImage().getImageUrl())
                        .sain(r.getMaladieDetectee() != null && r.getMaladieDetectee().equalsIgnoreCase("Healthy"))
                        .parcelleNom(r.getImage().getParcelle().getNom())
                        .typeCulture(r.getImage().getParcelle().getTypeCulture())
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
