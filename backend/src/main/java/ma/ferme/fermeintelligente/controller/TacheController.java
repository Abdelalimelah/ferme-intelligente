package ma.ferme.fermeintelligente.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.TacheDTO;
import ma.ferme.fermeintelligente.service.TacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/taches")
@RequiredArgsConstructor
public class TacheController {

    private final TacheService tacheService;

    @GetMapping
    public ResponseEntity<List<TacheDTO>> getAll() {
        return ResponseEntity.ok(tacheService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TacheDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tacheService.findById(id));
    }

    @GetMapping("/agriculteur/{agricId}")
    public ResponseEntity<List<TacheDTO>> getByAgriculteur(@PathVariable Long agricId) {
        return ResponseEntity.ok(tacheService.findByAgriculteur(agricId));
    }

    @GetMapping("/gestionnaire/{gestId}")
    public ResponseEntity<List<TacheDTO>> getByGestionnaire(@PathVariable Long gestId) {
        return ResponseEntity.ok(tacheService.findByGestionnaire(gestId));
    }

    @PostMapping
    public ResponseEntity<TacheDTO> create(@Valid @RequestBody TacheDTO dto) {
        return ResponseEntity.ok(tacheService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TacheDTO> update(@PathVariable Long id, @Valid @RequestBody TacheDTO dto) {
        return ResponseEntity.ok(tacheService.update(id, dto));
    }

    @PutMapping("/{id}/demarrer")
    public ResponseEntity<TacheDTO> markAsDemarree(@PathVariable Long id) {
        return ResponseEntity.ok(tacheService.markAsDemarree(id));
    }

    @PutMapping("/{id}/terminer")
    public ResponseEntity<TacheDTO> markAsTerminee(@PathVariable Long id) {
        return ResponseEntity.ok(tacheService.markAsTerminee(id));
    }
}
