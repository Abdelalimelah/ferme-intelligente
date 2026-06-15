package ma.ferme.fermeintelligente.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.FermeDTO;
import ma.ferme.fermeintelligente.service.FermeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fermes")
@RequiredArgsConstructor
public class FermeController {

    private final FermeService fermeService;

    @GetMapping
    public ResponseEntity<List<FermeDTO>> getAll() {
        return ResponseEntity.ok(fermeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FermeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fermeService.findById(id));
    }

    @GetMapping("/proprietaire/{propId}")
    public ResponseEntity<List<FermeDTO>> getByProprietaire(@PathVariable Long propId) {
        return ResponseEntity.ok(fermeService.findByProprietaire(propId));
    }

    @PostMapping
    public ResponseEntity<FermeDTO> create(@Valid @RequestBody FermeDTO dto) {
        return ResponseEntity.ok(fermeService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FermeDTO> update(@PathVariable Long id, @Valid @RequestBody FermeDTO dto) {
        return ResponseEntity.ok(fermeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fermeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
