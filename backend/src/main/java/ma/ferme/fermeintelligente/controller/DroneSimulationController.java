package ma.ferme.fermeintelligente.controller;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.DroneSimulationStatusDTO;
import ma.ferme.fermeintelligente.service.DroneSimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulation/drone")
@RequiredArgsConstructor
public class DroneSimulationController {

    private final DroneSimulationService droneSimulationService;

    @GetMapping("/status")
    public ResponseEntity<DroneSimulationStatusDTO> status() {
        return ResponseEntity.ok(droneSimulationService.status());
    }

    @PostMapping("/toggle")
    public ResponseEntity<DroneSimulationStatusDTO> toggle() {
        return ResponseEntity.ok(droneSimulationService.toggle());
    }
}
