package ma.ferme.fermeintelligente.controller;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.ParcelleDetailDTO;
import ma.ferme.fermeintelligente.service.AIClassificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Get all disease detection results for a parcelle.
     */
    @GetMapping("/diseases/parcelle/{parcelleId}")
    public ResponseEntity<List<ParcelleDetailDTO.DiseaseResultDTO>> getDiseasesByParcelle(@PathVariable Long parcelleId) {
        return ResponseEntity.ok(aiClassificationService.getDiseasesByParcelle(parcelleId));
    }
}
