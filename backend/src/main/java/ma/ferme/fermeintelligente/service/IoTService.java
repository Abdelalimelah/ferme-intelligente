package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ferme.fermeintelligente.dto.IoTDataRequest;
import ma.ferme.fermeintelligente.dto.IoTDataResponse;
import ma.ferme.fermeintelligente.entity.*;
import ma.ferme.fermeintelligente.enums.NiveauAlerte;
import ma.ferme.fermeintelligente.exception.ResourceNotFoundException;
import ma.ferme.fermeintelligente.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IoTService {

    private final CapteurRepository capteurRepository;
    private final DonneeCapteurRepository donneeCapteurRepository;
    private final ParcelleRepository parcelleRepository;
    private final SeuilAlerteRepository seuilAlerteRepository;
    private final AlerteRepository alerteRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "capteurs", allEntries = true),
        @CacheEvict(value = "capteurs_parcelle", allEntries = true)
    })
    public IoTDataResponse ingestData(IoTDataRequest request) {
        // Resolve capteur
        Capteur capteur = resolveCapteur(request);

        // Store sensor reading
        DonneeCapteur donnee = DonneeCapteur.builder()
                .valeur(request.getValeur())
                .unite(request.getUnite() != null ? request.getUnite() : capteur.getUnite())
                .dateReleve(LocalDateTime.now())
                .capteur(capteur)
                .build();
        donneeCapteurRepository.save(donnee);

        // Update capteur latest value
        capteur.setDerniereValeur(request.getValeur());
        capteur.setDerniereLecture(LocalDateTime.now());
        capteurRepository.save(capteur);

        // Check thresholds and generate alert if needed
        IoTDataResponse response = IoTDataResponse.builder()
                .id(donnee.getId())
                .valeur(request.getValeur())
                .unite(donnee.getUnite())
                .capteurType(capteur.getType())
                .capteurId(capteur.getId())
                .parcelleId(capteur.getParcelle().getId())
                .parcelleNom(capteur.getParcelle().getNom())
                .alerteGeneree(false)
                .build();

        checkThresholdAndAlert(capteur, request.getValeur(), response);

        // Broadcast live reading to all WebSocket subscribers
        // /topic/sensors           — all sensors (used by dashboard overview)
        // /topic/sensors/{parcelleId} — per-parcelle feed
        messagingTemplate.convertAndSend("/topic/sensors", response);
        messagingTemplate.convertAndSend(
                "/topic/sensors/" + capteur.getParcelle().getId(), response);

        return response;
    }

    private Capteur resolveCapteur(IoTDataRequest request) {
        if (request.getCapteurId() != null) {
            return capteurRepository.findById(request.getCapteurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Capteur not found: " + request.getCapteurId()));
        }
        // Resolve by parcelle + type
        if (request.getParcelleId() != null && request.getType() != null) {
            return capteurRepository.findByParcelleIdAndType(request.getParcelleId(), request.getType())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Capteur not found for parcelle " + request.getParcelleId() + " type " + request.getType()));
        }
        throw new IllegalArgumentException("Must provide capteurId or (parcelleId + type)");
    }

    private void checkThresholdAndAlert(Capteur capteur, Double valeur, IoTDataResponse response) {
        Double min = capteur.getValeurMin();
        Double max = capteur.getValeurMax();

        // Also check parcelle-specific thresholds
        Optional<SeuilAlerte> seuil = seuilAlerteRepository
                .findByTypeCapteurAndParcelleId(capteur.getType(), capteur.getParcelle().getId());

        if (seuil.isPresent()) {
            min = seuil.get().getValeurMin();
            max = seuil.get().getValeurMax();
        }

        if (min == null || max == null) return;

        String alertMessage = null;
        NiveauAlerte niveau = null;

        if (valeur < min) {
            double ecart = min - valeur;
            niveau = ecart > (max - min) * 0.5 ? NiveauAlerte.CRITIQUE : NiveauAlerte.WARNING;
            alertMessage = String.format("%s trop bas: %.1f %s (seuil min: %.1f)",
                    capteur.getType(), valeur, capteur.getUnite(), min);
        } else if (valeur > max) {
            double ecart = valeur - max;
            niveau = ecart > (max - min) * 0.5 ? NiveauAlerte.CRITIQUE : NiveauAlerte.WARNING;
            alertMessage = String.format("%s trop haut: %.1f %s (seuil max: %.1f)",
                    capteur.getType(), valeur, capteur.getUnite(), max);
        }

        if (alertMessage != null) {
            Alerte alerte = Alerte.builder()
                    .type("CAPTEUR")
                    .message(alertMessage)
                    .niveau(niveau)
                    .dateCreation(LocalDateTime.now())
                    .estLue(false)
                    .parcelle(capteur.getParcelle())
                    .build();
            alerteRepository.save(alerte);

            response.setAlerteGeneree(true);
            response.setAlerteMessage(alertMessage);
            log.warn("Alert generated for parcelle {}: {}", capteur.getParcelle().getNom(), alertMessage);
        }
    }
}
