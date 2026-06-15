package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.DonneeCapteurDTO;
import ma.ferme.fermeintelligente.entity.Capteur;
import ma.ferme.fermeintelligente.entity.DonneeCapteur;
import ma.ferme.fermeintelligente.exception.ResourceNotFoundException;
import ma.ferme.fermeintelligente.repository.CapteurRepository;
import ma.ferme.fermeintelligente.repository.DonneeCapteurRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DonneeCapteurService {

    private final DonneeCapteurRepository donneeCapteurRepository;
    private final CapteurRepository capteurRepository;

    public List<DonneeCapteurDTO> findByCapteur(Long capteurId) {
        return donneeCapteurRepository.findByCapteurIdOrderByDateReleveDesc(capteurId)
                .stream().map(this::toDTO).toList();
    }

    public List<DonneeCapteurDTO> findByDateRange(Long capteurId, LocalDateTime start, LocalDateTime end) {
        return donneeCapteurRepository.findByCapteurIdAndDateReleveBetween(capteurId, start, end)
                .stream().map(this::toDTO).toList();
    }

    public DonneeCapteurDTO create(DonneeCapteurDTO dto) {
        Capteur capteur = capteurRepository.findById(dto.getCapteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Capteur not found"));
        DonneeCapteur d = DonneeCapteur.builder()
                .valeur(dto.getValeur())
                .unite(dto.getUnite())
                .dateReleve(dto.getDateReleve() != null ? dto.getDateReleve() : LocalDateTime.now())
                .capteur(capteur)
                .build();
        return toDTO(donneeCapteurRepository.save(d));
    }

    private DonneeCapteurDTO toDTO(DonneeCapteur d) {
        return DonneeCapteurDTO.builder()
                .id(d.getId()).valeur(d.getValeur())
                .unite(d.getUnite()).dateReleve(d.getDateReleve())
                .capteurId(d.getCapteur().getId())
                .capteurType(d.getCapteur().getType())
                .build();
    }
}
