package ma.ferme.fermeintelligente.controller;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.ParcelleDetailDTO;
import ma.ferme.fermeintelligente.service.AIClassificationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIClassificationService aiClassificationService;

    /**
     * Trigger AI analysis on a specific drone image.
     */
    @PostMapping("/analyze/{imageId}")
    public ResponseEntity<ParcelleDetailDTO.DiseaseResultDTO> analyzeImage(@PathVariable Long imageId) {
        return ResponseEntity.ok(aiClassificationService.analyzeImage(imageId));
    }

    /**
     * Pick a random image from the dataset for the parcelle's plant type,
     * run AI analysis, store result, return it.
     */
    @PostMapping("/analyse/dataset/{parcelleId}")
    public ResponseEntity<ParcelleDetailDTO.DiseaseResultDTO> analyserDataset(@PathVariable Long parcelleId) {
        return ResponseEntity.ok(aiClassificationService.analyserDepuisDataset(parcelleId));
    }

    /**
     * Manual analysis: user uploads a picture for a chosen parcelle.
     */
    @PostMapping(value = "/analyse/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ParcelleDetailDTO.DiseaseResultDTO> analyserUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("parcelleId") Long parcelleId) {
        return ResponseEntity.ok(aiClassificationService.analyserDepuisUpload(parcelleId, file));
    }

    /**
     * Get all disease detection results for a parcelle.
     */
    @GetMapping("/diseases/parcelle/{parcelleId}")
    public ResponseEntity<List<ParcelleDetailDTO.DiseaseResultDTO>> getDiseasesByParcelle(@PathVariable Long parcelleId) {
        return ResponseEntity.ok(aiClassificationService.getDiseasesByParcelle(parcelleId));
    }
}
