package ma.ferme.fermeintelligente.controller;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.entity.ImageParcelle;
import ma.ferme.fermeintelligente.service.ImageParcelleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageParcelleController {

    private final ImageParcelleService imageParcelleService;

    @GetMapping("/parcelle/{parcelleId}")
    public ResponseEntity<List<ImageParcelle>> getByParcelle(@PathVariable Long parcelleId) {
        return ResponseEntity.ok(imageParcelleService.findByParcelle(parcelleId));
    }
}
