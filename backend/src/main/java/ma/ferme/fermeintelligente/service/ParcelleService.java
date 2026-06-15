package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.ParcelleDTO;
import ma.ferme.fermeintelligente.entity.Ferme;
import ma.ferme.fermeintelligente.entity.Parcelle;
import ma.ferme.fermeintelligente.exception.ResourceNotFoundException;
import ma.ferme.fermeintelligente.repository.FermeRepository;
import ma.ferme.fermeintelligente.repository.ParcelleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParcelleService {

    private final ParcelleRepository parcelleRepository;
    private final FermeRepository fermeRepository;

    public List<ParcelleDTO> findAll() {
        return parcelleRepository.findAll().stream().map(this::toDTO).toList();
    }

    public ParcelleDTO findById(Long id) {
        return toDTO(getById(id));
    }

    public List<ParcelleDTO> findByFerme(Long fermeId) {
        return parcelleRepository.findByFermeId(fermeId).stream().map(this::toDTO).toList();
    }

    public ParcelleDTO create(ParcelleDTO dto) {
        Ferme ferme = fermeRepository.findById(dto.getFermeId())
                .orElseThrow(() -> new ResourceNotFoundException("Ferme not found"));
        Parcelle p = Parcelle.builder()
                .nom(dto.getNom())
                .surface(dto.getSurface())
                .typeCulture(dto.getTypeCulture())
                .coordonneesGps(dto.getCoordonneesGps())
                .ferme(ferme)
                .build();
        return toDTO(parcelleRepository.save(p));
    }

    public ParcelleDTO update(Long id, ParcelleDTO dto) {
        Parcelle p = getById(id);
        p.setNom(dto.getNom());
        p.setSurface(dto.getSurface());
        p.setTypeCulture(dto.getTypeCulture());
        p.setCoordonneesGps(dto.getCoordonneesGps());
        return toDTO(parcelleRepository.save(p));
    }

    public void delete(Long id) {
        parcelleRepository.deleteById(id);
    }

    private Parcelle getById(Long id) {
        return parcelleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parcelle not found with id " + id));
    }

    private ParcelleDTO toDTO(Parcelle p) {
        return ParcelleDTO.builder()
                .id(p.getId()).nom(p.getNom())
                .surface(p.getSurface()).typeCulture(p.getTypeCulture())
                .coordonneesGps(p.getCoordonneesGps())
                .fermeId(p.getFerme().getId())
                .fermeNom(p.getFerme().getNom())
                .nombreCapteurs(p.getCapteurs() != null ? p.getCapteurs().size() : 0)
                .nombreAgriculteurs(p.getAgriculteurs() != null ? p.getAgriculteurs().size() : 0)
                .build();
    }
}
