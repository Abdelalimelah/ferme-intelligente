package ma.ferme.fermeintelligente.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.CapteurDTO;
import ma.ferme.fermeintelligente.service.CapteurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/capteurs")
@RequiredArgsConstructor
public class CapteurController {

    private final CapteurService capteurService;

    @GetMapping
    public ResponseEntity<List<CapteurDTO>> getAll() {
        return ResponseEntity.ok(capteurService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CapteurDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(capteurService.findById(id));
    }

    @GetMapping("/parcelle/{parcelleId}")
    public ResponseEntity<List<CapteurDTO>> getByParcelle(@PathVariable Long parcelleId) {
        return ResponseEntity.ok(capteurService.findByParcelle(parcelleId));
    }

    @PostMapping
    public ResponseEntity<CapteurDTO> create(@Valid @RequestBody CapteurDTO dto) {
        return ResponseEntity.ok(capteurService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CapteurDTO> update(@PathVariable Long id, @Valid @RequestBody CapteurDTO dto) {
        return ResponseEntity.ok(capteurService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        capteurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
