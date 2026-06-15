package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.FermeDTO;
import ma.ferme.fermeintelligente.entity.Ferme;
import ma.ferme.fermeintelligente.entity.Utilisateur;
import ma.ferme.fermeintelligente.exception.ResourceNotFoundException;
import ma.ferme.fermeintelligente.repository.FermeRepository;
import ma.ferme.fermeintelligente.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FermeService {

    private final FermeRepository fermeRepository;
    private final UtilisateurRepository utilisateurRepository;

    public List<FermeDTO> findAll() {
        return fermeRepository.findAll().stream().map(this::toDTO).toList();
    }

    public FermeDTO findById(Long id) {
        return toDTO(getById(id));
    }

    public List<FermeDTO> findByProprietaire(Long proprietaireId) {
        return fermeRepository.findByProprietaireId(proprietaireId).stream().map(this::toDTO).toList();
    }

    public FermeDTO create(FermeDTO dto) {
        Utilisateur proprietaire = utilisateurRepository.findById(dto.getProprietaireId())
                .orElseThrow(() -> new ResourceNotFoundException("Proprietaire not found"));
        Ferme ferme = Ferme.builder()
                .nom(dto.getNom())
                .localisation(dto.getLocalisation())
                .surface(dto.getSurface())
                .dateCreation(LocalDateTime.now())
                .proprietaire(proprietaire)
                .build();
        return toDTO(fermeRepository.save(ferme));
    }

    public FermeDTO update(Long id, FermeDTO dto) {
        Ferme ferme = getById(id);
        ferme.setNom(dto.getNom());
        ferme.setLocalisation(dto.getLocalisation());
        ferme.setSurface(dto.getSurface());
        return toDTO(fermeRepository.save(ferme));
    }

    public void delete(Long id) {
        fermeRepository.deleteById(id);
    }

    private Ferme getById(Long id) {
        return fermeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ferme not found with id " + id));
    }

    private FermeDTO toDTO(Ferme f) {
        return FermeDTO.builder()
                .id(f.getId()).nom(f.getNom())
                .localisation(f.getLocalisation()).surface(f.getSurface())
                .dateCreation(f.getDateCreation())
                .proprietaireId(f.getProprietaire().getId())
                .proprietaireNom(f.getProprietaire().getPrenom() + " " + f.getProprietaire().getNom())
                .nombreParcelles(f.getParcelles() != null ? f.getParcelles().size() : 0)
                .nombreGestionnaires(f.getGestionnaires() != null ? f.getGestionnaires().size() : 0)
                .build();
    }
}
