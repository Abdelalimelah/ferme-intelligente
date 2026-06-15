package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.TacheDTO;
import ma.ferme.fermeintelligente.entity.Parcelle;
import ma.ferme.fermeintelligente.entity.Tache;
import ma.ferme.fermeintelligente.entity.Utilisateur;
import ma.ferme.fermeintelligente.enums.StatutTache;
import ma.ferme.fermeintelligente.exception.ResourceNotFoundException;
import ma.ferme.fermeintelligente.repository.ParcelleRepository;
import ma.ferme.fermeintelligente.repository.TacheRepository;
import ma.ferme.fermeintelligente.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TacheService {

    private final TacheRepository tacheRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ParcelleRepository parcelleRepository;

    public List<TacheDTO> findAll() {
        return tacheRepository.findAll().stream().map(this::toDTO).toList();
    }

    public TacheDTO findById(Long id) {
        return toDTO(getById(id));
    }

    public List<TacheDTO> findByAgriculteur(Long agriculteurId) {
        return tacheRepository.findByAgriculteurIdOrderByDateCreationDesc(agriculteurId)
                .stream().map(this::toDTO).toList();
    }

    public List<TacheDTO> findByGestionnaire(Long gestionnaireId) {
        return tacheRepository.findByGestionnaireIdOrderByDateCreationDesc(gestionnaireId)
                .stream().map(this::toDTO).toList();
    }

    public TacheDTO create(TacheDTO dto) {
        Utilisateur agriculteur = utilisateurRepository.findById(dto.getAgriculteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Agriculteur not found"));
        Utilisateur gestionnaire = utilisateurRepository.findById(dto.getGestionnaireId())
                .orElseThrow(() -> new ResourceNotFoundException("Gestionnaire not found"));
        Parcelle parcelle = dto.getParcelleId() != null
                ? parcelleRepository.findById(dto.getParcelleId()).orElse(null)
                : null;

        Tache t = Tache.builder()
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .statut(StatutTache.A_FAIRE)
                .priorite(dto.getPriorite())
                .dateCreation(LocalDateTime.now())
                .dateEcheance(dto.getDateEcheance())
                .agriculteur(agriculteur)
                .gestionnaire(gestionnaire)
                .parcelle(parcelle)
                .build();
        return toDTO(tacheRepository.save(t));
    }

    public TacheDTO update(Long id, TacheDTO dto) {
        Tache t = getById(id);
        t.setTitre(dto.getTitre());
        t.setDescription(dto.getDescription());
        if (dto.getStatut() != null) t.setStatut(dto.getStatut());
        if (dto.getPriorite() != null) t.setPriorite(dto.getPriorite());
        t.setDateEcheance(dto.getDateEcheance());
        return toDTO(tacheRepository.save(t));
    }

    public TacheDTO markAsDemarree(Long id) {
        Tache t = getById(id);
        t.setStatut(StatutTache.EN_COURS);
        return toDTO(tacheRepository.save(t));
    }

    public TacheDTO markAsTerminee(Long id) {
        Tache t = getById(id);
        t.setStatut(StatutTache.TERMINEE);
        t.setDateTerminee(LocalDateTime.now());
        return toDTO(tacheRepository.save(t));
    }

    private Tache getById(Long id) {
        return tacheRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tache not found with id " + id));
    }

    private TacheDTO toDTO(Tache t) {
        return TacheDTO.builder()
                .id(t.getId()).titre(t.getTitre())
                .description(t.getDescription())
                .statut(t.getStatut()).priorite(t.getPriorite())
                .dateCreation(t.getDateCreation())
                .dateEcheance(t.getDateEcheance())
                .dateTerminee(t.getDateTerminee())
                .agriculteurId(t.getAgriculteur().getId())
                .agriculteurNom(t.getAgriculteur().getPrenom() + " " + t.getAgriculteur().getNom())
                .gestionnaireId(t.getGestionnaire().getId())
                .gestionnaireNom(t.getGestionnaire().getPrenom() + " " + t.getGestionnaire().getNom())
                .parcelleId(t.getParcelle() != null ? t.getParcelle().getId() : null)
                .parcelleNom(t.getParcelle() != null ? t.getParcelle().getNom() : null)
                .build();
    }
}
