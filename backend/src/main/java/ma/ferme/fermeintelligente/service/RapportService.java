package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.RapportDTO;
import ma.ferme.fermeintelligente.entity.Rapport;
import ma.ferme.fermeintelligente.entity.Utilisateur;
import ma.ferme.fermeintelligente.enums.StatutRapport;
import ma.ferme.fermeintelligente.enums.TypeRapport;
import ma.ferme.fermeintelligente.exception.ResourceNotFoundException;
import ma.ferme.fermeintelligente.repository.RapportRepository;
import ma.ferme.fermeintelligente.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RapportService {

    private final RapportRepository rapportRepository;
    private final UtilisateurRepository utilisateurRepository;

    public List<RapportDTO> findAll() {
        return rapportRepository.findAllByOrderByDateCreationDesc().stream().map(this::toDTO).toList();
    }

    public RapportDTO findById(Long id) {
        return toDTO(getById(id));
    }

    public List<RapportDTO> findByAuteur(Long auteurId) {
        return rapportRepository.findByAuteurIdOrderByDateCreationDesc(auteurId)
                .stream().map(this::toDTO).toList();
    }

    public List<RapportDTO> findByType(TypeRapport type) {
        return rapportRepository.findByTypeOrderByDateCreationDesc(type)
                .stream().map(this::toDTO).toList();
    }

    public RapportDTO create(RapportDTO dto) {
        Utilisateur auteur = utilisateurRepository.findById(dto.getAuteurId())
                .orElseThrow(() -> new ResourceNotFoundException("Auteur not found"));
        Rapport r = Rapport.builder()
                .type(dto.getType())
                .sujet(dto.getSujet())
                .contenu(dto.getContenu())
                .dateCreation(LocalDateTime.now())
                .statut(StatutRapport.NON_TRAITE)
                .auteur(auteur)
                .build();
        return toDTO(rapportRepository.save(r));
    }

    public RapportDTO updateStatut(Long id, StatutRapport statut) {
        Rapport r = getById(id);
        r.setStatut(statut);
        return toDTO(rapportRepository.save(r));
    }

    private Rapport getById(Long id) {
        return rapportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rapport not found with id " + id));
    }

    private RapportDTO toDTO(Rapport r) {
        return RapportDTO.builder()
                .id(r.getId()).type(r.getType())
                .sujet(r.getSujet()).contenu(r.getContenu())
                .dateCreation(r.getDateCreation()).statut(r.getStatut())
                .auteurId(r.getAuteur().getId())
                .auteurNom(r.getAuteur().getPrenom() + " " + r.getAuteur().getNom())
                .build();
    }
}
