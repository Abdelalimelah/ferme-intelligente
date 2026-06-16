package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ferme.fermeintelligente.dto.DroneSimulationStatusDTO;
import ma.ferme.fermeintelligente.dto.ParcelleDetailDTO;
import ma.ferme.fermeintelligente.entity.Drone;
import ma.ferme.fermeintelligente.entity.Parcelle;
import ma.ferme.fermeintelligente.repository.DroneRepository;
import ma.ferme.fermeintelligente.repository.ParcelleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simulates a drone autonomously flying over random parcelles and capturing
 * images from the PlantVillage dataset for AI analysis. Toggled on/off from
 * the UI; runs on a fixed schedule while enabled.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DroneSimulationService {

    private static final long MIN_DELAY_MS = 20_000;
    private static final long JITTER_MS = 10_000; // makes the effective interval 20-30s
    private static final int INTERVAL_SECONDS = 25; // midpoint, shown to clients

    private final ParcelleRepository parcelleRepository;
    private final DroneRepository droneRepository;
    private final AIClassificationService aiClassificationService;

    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private final Random random = new Random();

    private volatile LocalDateTime lastRun;
    private volatile String lastParcelleNom;
    private volatile String lastResultat;

    public DroneSimulationStatusDTO status() {
        return DroneSimulationStatusDTO.builder()
                .enabled(enabled.get())
                .intervalSeconds(INTERVAL_SECONDS)
                .lastRun(lastRun)
                .lastParcelleNom(lastParcelleNom)
                .lastResultat(lastResultat)
                .build();
    }

    public DroneSimulationStatusDTO toggle() {
        enabled.set(!enabled.get());
        log.info("Drone simulation {}", enabled.get() ? "ENABLED" : "DISABLED");
        return status();
    }

    @Scheduled(fixedDelay = MIN_DELAY_MS)
    public void autoCapture() {
        if (!enabled.get()) return;

        try {
            Thread.sleep((long) (random.nextDouble() * JITTER_MS)); // 20-30s effective spacing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        if (!enabled.get()) return; // re-check in case toggled off during jitter

        List<Parcelle> parcelles = parcelleRepository.findAll();
        if (parcelles.isEmpty()) return;

        Parcelle parcelle = parcelles.get(random.nextInt(parcelles.size()));
        Drone drone = droneRepository.findAll().stream().findFirst().orElse(null);

        try {
            ParcelleDetailDTO.DiseaseResultDTO result =
                    aiClassificationService.analyserDepuisDataset(parcelle.getId(), drone);
            lastRun = LocalDateTime.now();
            lastParcelleNom = parcelle.getNom();
            lastResultat = result.getMaladieFr() != null ? result.getMaladieFr() : result.getMaladieDetectee();
            log.info("Drone auto-capture: parcelle={} resultat={}", parcelle.getNom(), lastResultat);
        } catch (Exception e) {
            log.error("Drone auto-capture failed for parcelle {}: {}", parcelle.getId(), e.getMessage());
        }
    }
}
