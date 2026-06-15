package ma.ferme.fermeintelligente.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.UtilisateurDTO;
import ma.ferme.fermeintelligente.enums.Role;
import ma.ferme.fermeintelligente.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    public ResponseEntity<List<UtilisateurDTO>> getAll() {
        return ResponseEntity.ok(utilisateurService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.findById(id));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UtilisateurDTO>> getByRole(@PathVariable Role role) {
        return ResponseEntity.ok(utilisateurService.findByRole(role));
    }

    @PostMapping
    public ResponseEntity<UtilisateurDTO> create(@Valid @RequestBody UtilisateurDTO dto) {
        return ResponseEntity.ok(utilisateurService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> update(@PathVariable Long id, @Valid @RequestBody UtilisateurDTO dto) {
        return ResponseEntity.ok(utilisateurService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        utilisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/fermes/{fermeId}")
    public ResponseEntity<Void> assignGestionnaireToFerme(@PathVariable Long userId, @PathVariable Long fermeId) {
        utilisateurService.assignGestionnaireToFerme(userId, fermeId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/parcelles/{parcelleId}")
    public ResponseEntity<Void> assignAgriculteurToParcelle(@PathVariable Long userId, @PathVariable Long parcelleId) {
        utilisateurService.assignAgriculteurToParcelle(userId, parcelleId);
        return ResponseEntity.ok().build();
    }
}
