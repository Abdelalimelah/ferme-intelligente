package ma.ferme.fermeintelligente.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DroneSimulationStatusDTO {
    private boolean enabled;
    private int intervalSeconds;
    private LocalDateTime lastRun;
    private String lastParcelleNom;
    private String lastResultat;
}
