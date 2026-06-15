package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.dto.UtilisateurDTO;
import ma.ferme.fermeintelligente.entity.Ferme;
import ma.ferme.fermeintelligente.entity.Parcelle;
import ma.ferme.fermeintelligente.entity.Utilisateur;
import ma.ferme.fermeintelligente.enums.Role;
import ma.ferme.fermeintelligente.exception.ResourceNotFoundException;
import ma.ferme.fermeintelligente.repository.FermeRepository;
import ma.ferme.fermeintelligente.repository.ParcelleRepository;
import ma.ferme.fermeintelligente.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final FermeRepository fermeRepository;
    private final ParcelleRepository parcelleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public List<UtilisateurDTO> findAll() {
        return utilisateurRepository.findAll().stream().map(this::toDTO).toList();
    }

    public UtilisateurDTO findById(Long id) {
        return toDTO(getById(id));
    }

    public List<UtilisateurDTO> findByRole(Role role) {
        return utilisateurRepository.findByRole(role).stream().map(this::toDTO).toList();
    }

    public UtilisateurDTO create(UtilisateurDTO dto) {
        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        // Generate a temporary password the owner communicates to the new user
        String tempPassword = generateTempPassword();
        Utilisateur user = Utilisateur.builder()
                .nom(dto.getNom()).prenom(dto.getPrenom())
                .email(dto.getEmail())
                .motDePasse(passwordEncoder.encode(tempPassword))
                .telephone(dto.getTelephone())
                .role(dto.getRole())
                .statut("ACTIF")
                .dateCreation(LocalDateTime.now())
                .mustResetPassword(true)
                .build();
        UtilisateurDTO result = toDTO(utilisateurRepository.save(user));
        // Return the plaintext temp password ONCE so the owner can share it
        result.setTemporaryPassword(tempPassword);
        // Also send it by email (async — non-blocking)
        emailService.sendTempPasswordEmail(dto.getEmail(), dto.getPrenom(), tempPassword);
        return result;
    }

    private String generateTempPassword() {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public UtilisateurDTO update(Long id, UtilisateurDTO dto) {
        Utilisateur user = getById(id);
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setEmail(dto.getEmail());
        user.setTelephone(dto.getTelephone());
        if (dto.getRole() != null) user.setRole(dto.getRole());
        if (dto.getMotDePasse() != null && !dto.getMotDePasse().isBlank()) {
            user.setMotDePasse(passwordEncoder.encode(dto.getMotDePasse()));
        }
        return toDTO(utilisateurRepository.save(user));
    }

    public void delete(Long id) {
        utilisateurRepository.deleteById(id);
    }

    @Transactional
    public void assignGestionnaireToFerme(Long userId, Long fermeId) {
        Utilisateur user = getById(userId);
        Ferme ferme = fermeRepository.findById(fermeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ferme not found"));
        user.getFermesGerees().add(ferme);
        utilisateurRepository.save(user);
    }

    @Transactional
    public void assignAgriculteurToParcelle(Long userId, Long parcelleId) {
        Utilisateur user = getById(userId);
        Parcelle parcelle = parcelleRepository.findById(parcelleId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcelle not found"));
        user.getParcellesAssignees().add(parcelle);
        utilisateurRepository.save(user);
    }

    private Utilisateur getById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur not found with id " + id));
    }

    private UtilisateurDTO toDTO(Utilisateur u) {
        return UtilisateurDTO.builder()
                .id(u.getId()).nom(u.getNom()).prenom(u.getPrenom())
                .email(u.getEmail()).telephone(u.getTelephone())
                .role(u.getRole()).statut(u.getStatut())
                .dateCreation(u.getDateCreation())
                .mustResetPassword(u.getMustResetPassword())
                .build();
    }
}
