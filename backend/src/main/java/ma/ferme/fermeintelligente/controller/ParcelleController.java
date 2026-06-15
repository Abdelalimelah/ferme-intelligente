package ma.ferme.fermeintelligente.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.DonneeCapteurDTO;
import ma.ferme.fermeintelligente.dto.ParcelleDTO;
import ma.ferme.fermeintelligente.dto.ParcelleDetailDTO;
import ma.ferme.fermeintelligente.service.DonneeCapteurService;
import ma.ferme.fermeintelligente.service.ParcelleDetailService;
import ma.ferme.fermeintelligente.service.ParcelleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parcelles")
@RequiredArgsConstructor
public class ParcelleController {

    private final ParcelleService parcelleService;
    private final ParcelleDetailService parcelleDetailService;
    private final DonneeCapteurService donneeCapteurService;

    @GetMapping
    public ResponseEntity<List<ParcelleDTO>> getAll() {
        return ResponseEntity.ok(parcelleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParcelleDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(parcelleService.findById(id));
    }

    @GetMapping("/ferme/{fermeId}")
    public ResponseEntity<List<ParcelleDTO>> getByFerme(@PathVariable Long fermeId) {
        return ResponseEntity.ok(parcelleService.findByFerme(fermeId));
    }

    @PostMapping
    public ResponseEntity<ParcelleDTO> create(@Valid @RequestBody ParcelleDTO dto) {
        return ResponseEntity.ok(parcelleService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParcelleDTO> update(@PathVariable Long id, @Valid @RequestBody ParcelleDTO dto) {
        return ResponseEntity.ok(parcelleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        parcelleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get full parcel detail with live sensors, alerts, diseases, images.
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<ParcelleDetailDTO> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(parcelleDetailService.getDetail(id));
    }

    /**
     * Get sensor history for a specific capteur on this parcel.
     */
    @GetMapping("/{parcelleId}/capteurs/{capteurId}/history")
    public ResponseEntity<List<DonneeCapteurDTO>> getSensorHistory(
            @PathVariable Long parcelleId, @PathVariable Long capteurId) {
        return ResponseEntity.ok(donneeCapteurService.findByCapteur(capteurId));
    }
}
