package ma.ferme.fermeintelligente.controller;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.DonneeCapteurDTO;
import ma.ferme.fermeintelligente.service.DonneeCapteurService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/donnees-capteur")
@RequiredArgsConstructor
public class DonneeCapteurController {

    private final DonneeCapteurService donneeCapteurService;

    @GetMapping("/capteur/{capteurId}")
    public ResponseEntity<List<DonneeCapteurDTO>> getByCapteur(@PathVariable Long capteurId) {
        return ResponseEntity.ok(donneeCapteurService.findByCapteur(capteurId));
    }

    @GetMapping("/capteur/{capteurId}/range")
    public ResponseEntity<List<DonneeCapteurDTO>> getByDateRange(
            @PathVariable Long capteurId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(donneeCapteurService.findByDateRange(capteurId, start, end));
    }

    @PostMapping
    public ResponseEntity<DonneeCapteurDTO> create(@RequestBody DonneeCapteurDTO dto) {
        return ResponseEntity.ok(donneeCapteurService.create(dto));
    }
}
