package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.CapteurDTO;
import ma.ferme.fermeintelligente.entity.Capteur;
import ma.ferme.fermeintelligente.entity.DonneeCapteur;
import ma.ferme.fermeintelligente.entity.Parcelle;
import ma.ferme.fermeintelligente.exception.ResourceNotFoundException;
import ma.ferme.fermeintelligente.repository.CapteurRepository;
import ma.ferme.fermeintelligente.repository.DonneeCapteurRepository;
import ma.ferme.fermeintelligente.repository.ParcelleRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CapteurService {

    private final CapteurRepository capteurRepository;
    private final ParcelleRepository parcelleRepository;
    private final DonneeCapteurRepository donneeCapteurRepository;

    @Cacheable("capteurs")
    public List<CapteurDTO> findAll() {
        return capteurRepository.findAll().stream().map(this::toDTO).toList();
    }

    public CapteurDTO findById(Long id) {
        return toDTO(getById(id));
    }

    @Cacheable(value = "capteurs_parcelle", key = "#parcelleId")
    public List<CapteurDTO> findByParcelle(Long parcelleId) {
        return capteurRepository.findByParcelleId(parcelleId).stream().map(this::toDTO).toList();
    }

    @Caching(evict = {
        @CacheEvict(value = "capteurs", allEntries = true),
        @CacheEvict(value = "capteurs_parcelle", allEntries = true)
    })
    public CapteurDTO create(CapteurDTO dto) {
        Parcelle parcelle = parcelleRepository.findById(dto.getParcelleId())
                .orElseThrow(() -> new ResourceNotFoundException("Parcelle not found"));
        Capteur c = Capteur.builder()
                .type(dto.getType())
                .unite(dto.getUnite())
                .statut(dto.getStatut() != null ? dto.getStatut() : "ACTIF")
                .dateInstallation(dto.getDateInstallation())
                .parcelle(parcelle)
                .build();
        return toDTO(capteurRepository.save(c));
    }

    @Caching(evict = {
        @CacheEvict(value = "capteurs", allEntries = true),
        @CacheEvict(value = "capteurs_parcelle", allEntries = true)
    })
    public CapteurDTO update(Long id, CapteurDTO dto) {
        Capteur c = getById(id);
        if (dto.getType() != null) c.setType(dto.getType());
        if (dto.getUnite() != null) c.setUnite(dto.getUnite());
        if (dto.getStatut() != null) c.setStatut(dto.getStatut());
        if (dto.getDateInstallation() != null) c.setDateInstallation(dto.getDateInstallation());
        if (dto.getParcelleId() != null && !dto.getParcelleId().equals(c.getParcelle().getId())) {
            Parcelle parcelle = parcelleRepository.findById(dto.getParcelleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parcelle not found"));
            c.setParcelle(parcelle);
        }
        return toDTO(capteurRepository.save(c));
    }

    @Caching(evict = {
        @CacheEvict(value = "capteurs", allEntries = true),
        @CacheEvict(value = "capteurs_parcelle", allEntries = true)
    })
    public void delete(Long id) {
        capteurRepository.delete(getById(id));
    }

    private Capteur getById(Long id) {
        return capteurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Capteur not found with id " + id));
    }

    private CapteurDTO toDTO(Capteur c) {
        Double latestVal = donneeCapteurRepository.findFirstByCapteurIdOrderByDateReleveDesc(c.getId())
                .map(DonneeCapteur::getValeur).orElse(null);
        return CapteurDTO.builder()
                .id(c.getId()).type(c.getType())
                .unite(c.getUnite()).statut(c.getStatut())
                .dateInstallation(c.getDateInstallation())
                .parcelleId(c.getParcelle().getId())
                .parcelleNom(c.getParcelle().getNom())
                .derniereValeur(latestVal != null ? latestVal : c.getDerniereValeur())
                .derniereLecture(c.getDerniereLecture())
                .build();
    }
}
