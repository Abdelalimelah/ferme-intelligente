package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.AlerteDTO;
import ma.ferme.fermeintelligente.dto.ParcelleDetailDTO;
import ma.ferme.fermeintelligente.entity.*;
import ma.ferme.fermeintelligente.exception.ResourceNotFoundException;
import ma.ferme.fermeintelligente.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParcelleDetailService {

    private final ParcelleRepository parcelleRepository;
    private final CapteurRepository capteurRepository;
    private final DonneeCapteurRepository donneeCapteurRepository;
    private final AlerteRepository alerteRepository;
    private final ImageParcelleRepository imageParcelleRepository;
    private final ResultatAnalyseRepository resultatAnalyseRepository;
    private final SeuilAlerteRepository seuilAlerteRepository;

    public ParcelleDetailDTO getDetail(Long parcelleId) {
        Parcelle parcelle = parcelleRepository.findById(parcelleId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcelle not found: " + parcelleId));

        List<Capteur> capteurs = capteurRepository.findByParcelleId(parcelleId);
        List<Alerte> alertes = alerteRepository.findByParcelleIdOrderByDateCreationDesc(parcelleId);
        List<ImageParcelle> images = imageParcelleRepository.findTop10ByParcelleIdOrderByDateCaptureDesc(parcelleId);
        List<ResultatAnalyse> resultats = resultatAnalyseRepository.findByParcelleIdOrderByDateDesc(parcelleId);

        // Build capteur live DTOs
        List<ParcelleDetailDTO.CapteurLiveDTO> capteurDTOs = capteurs.stream().map(c -> {
            boolean enAlerte = false;
            if (c.getDerniereValeur() != null && c.getValeurMin() != null && c.getValeurMax() != null) {
                enAlerte = c.getDerniereValeur() < c.getValeurMin() || c.getDerniereValeur() > c.getValeurMax();
            }
            return ParcelleDetailDTO.CapteurLiveDTO.builder()
                    .id(c.getId())
                    .type(c.getType())
                    .unite(c.getUnite())
                    .statut(c.getStatut())
                    .derniereValeur(c.getDerniereValeur())
                    .derniereLecture(c.getDerniereLecture())
                    .valeurMin(c.getValeurMin())
                    .valeurMax(c.getValeurMax())
                    .enAlerte(enAlerte)
                    .build();
        }).toList();

        // Build alerte DTOs (latest 20)
        List<AlerteDTO> alerteDTOs = alertes.stream().limit(20).map(a -> AlerteDTO.builder()
                .id(a.getId())
                .type(a.getType())
                .message(a.getMessage())
                .niveau(a.getNiveau())
                .dateCreation(a.getDateCreation())
                .estLue(a.getEstLue())
                .parcelleId(parcelleId)
                .parcelleNom(parcelle.getNom())
                .resultatId(a.getResultat() != null ? a.getResultat().getId() : null)
                .build()
        ).toList();

        // Build disease results
        List<ParcelleDetailDTO.DiseaseResultDTO> diseaseDTOs = resultats.stream()
                .filter(r -> r.getMaladieDetectee() != null && !r.getMaladieDetectee().equals("Healthy"))
                .limit(10)
                .map(r -> ParcelleDetailDTO.DiseaseResultDTO.builder()
                        .id(r.getId())
                        .maladieDetectee(r.getMaladieDetectee())
                        .niveauConfiance(r.getNiveauConfiance())
                        .dateAnalyse(r.getDateAnalyse())
                        .recommandation(r.getRecommandation())
                        .imagePath(r.getImage() != null ? r.getImage().getCheminFichier() : null)
                        .build())
                .toList();

        // Build image DTOs
        List<ParcelleDetailDTO.ImageDTO> imageDTOs = images.stream().map(img -> {
            boolean analysee = img.getResultatAnalyse() != null;
            String maladie = analysee ? img.getResultatAnalyse().getMaladieDetectee() : null;
            return ParcelleDetailDTO.ImageDTO.builder()
                    .id(img.getId())
                    .cheminFichier(img.getCheminFichier())
                    .dateCapture(img.getDateCapture())
                    .resolution(img.getResolution())
                    .analysee(analysee)
                    .maladieDetectee(maladie)
                    .build();
        }).toList();

        // Compute stats
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        Optional<Double> tempAvg = donneeCapteurRepository.avgByParcelleAndTypeSince(parcelleId, "Temperature", last24h);
        Optional<Double> humAvg = donneeCapteurRepository.avgByParcelleAndTypeSince(parcelleId, "Humidite", last24h);
        Optional<Double> phAvg = donneeCapteurRepository.avgByParcelleAndTypeSince(parcelleId, "pH", last24h);

        ParcelleDetailDTO.SensorStatsDTO stats = ParcelleDetailDTO.SensorStatsDTO.builder()
                .tempMoyenne(tempAvg.orElse(null))
                .humiditeMoyenne(humAvg.orElse(null))
                .phMoyen(phAvg.orElse(null))
                .totalAlertes(alertes.size())
                .alertesCritiques((int) alertes.stream().filter(a -> a.getNiveau() != null && a.getNiveau().name().equals("CRITIQUE")).count())
                .maladiesDetectees(diseaseDTOs.size())
                .build();

        return ParcelleDetailDTO.builder()
                .id(parcelle.getId())
                .nom(parcelle.getNom())
                .surface(parcelle.getSurface())
                .typeCulture(parcelle.getTypeCulture())
                .coordonneesGps(parcelle.getCoordonneesGps())
                .latitude(parcelle.getLatitude())
                .longitude(parcelle.getLongitude())
                .fermeNom(parcelle.getFerme().getNom())
                .capteurs(capteurDTOs)
                .alertesRecentes(alerteDTOs)
                .maladiesDetectees(diseaseDTOs)
                .imagesRecentes(imageDTOs)
                .stats(stats)
                .build();
    }
}
