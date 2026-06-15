package ma.ferme.fermeintelligente.controller;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.AlerteDTO;
import ma.ferme.fermeintelligente.service.AlerteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertes")
@RequiredArgsConstructor
public class AlerteController {

    private final AlerteService alerteService;

    @GetMapping
    public ResponseEntity<List<AlerteDTO>> getAll() {
        return ResponseEntity.ok(alerteService.findAll());
    }

    @GetMapping("/unread")
    public ResponseEntity<List<AlerteDTO>> getUnread() {
        return ResponseEntity.ok(alerteService.findUnread());
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnread() {
        return ResponseEntity.ok(alerteService.countUnread());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        alerteService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<AlerteDTO> create(@RequestBody AlerteDTO dto) {
        return ResponseEntity.ok(alerteService.create(dto));
    }
}
