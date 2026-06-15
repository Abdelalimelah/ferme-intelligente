package ma.ferme.fermeintelligente.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.RapportDTO;
import ma.ferme.fermeintelligente.enums.StatutRapport;
import ma.ferme.fermeintelligente.enums.TypeRapport;
import ma.ferme.fermeintelligente.service.RapportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
public class RapportController {

    private final RapportService rapportService;

    @GetMapping
    public ResponseEntity<List<RapportDTO>> getAll() {
        return ResponseEntity.ok(rapportService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RapportDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(rapportService.findById(id));
    }

    @GetMapping("/auteur/{auteurId}")
    public ResponseEntity<List<RapportDTO>> getByAuteur(@PathVariable Long auteurId) {
        return ResponseEntity.ok(rapportService.findByAuteur(auteurId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<RapportDTO>> getByType(@PathVariable TypeRapport type) {
        return ResponseEntity.ok(rapportService.findByType(type));
    }

    @PostMapping
    public ResponseEntity<RapportDTO> create(@Valid @RequestBody RapportDTO dto) {
        return ResponseEntity.ok(rapportService.create(dto));
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<RapportDTO> updateStatut(@PathVariable Long id, @RequestParam StatutRapport statut) {
        return ResponseEntity.ok(rapportService.updateStatut(id, statut));
    }
}
