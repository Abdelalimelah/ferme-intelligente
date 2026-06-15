package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.config.JwtUtils;
import ma.ferme.fermeintelligente.dto.*;
import ma.ferme.fermeintelligente.entity.Utilisateur;
import ma.ferme.fermeintelligente.repository.UtilisateurRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtils              jwtUtils;
    private final RefreshTokenService   refreshTokenService;
    private final EmailService          emailService;

    public LoginResponse login(LoginRequest request) {
        Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getMotDePasse(), user.getMotDePasse())) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        String accessToken  = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken, toDTO(user));
    }

    /** Rotate refresh token and issue a new access token. */
    @Transactional
    public RefreshTokenResponse refresh(String oldRefreshToken) {
        Utilisateur user = refreshTokenService.validateAndGetUser(oldRefreshToken);
        // Rotate: revoke old, issue new
        String newAccessToken  = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = refreshTokenService.createRefreshToken(user);
        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    /** Logout: revoke the specific refresh token. */
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }

    public UtilisateurDTO register(UtilisateurDTO dto) {
        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }
        Utilisateur user = Utilisateur.builder()
                .nom(dto.getNom()).prenom(dto.getPrenom())
                .email(dto.getEmail())
                .motDePasse(passwordEncoder.encode(dto.getMotDePasse()))
                .telephone(dto.getTelephone())
                .role(dto.getRole())
                .statut("ACTIF")
                .dateCreation(java.time.LocalDateTime.now())
                .build();
        return toDTO(utilisateurRepository.save(user));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Utilisateur user;
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user = utilisateurRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Utilisateur introuvable"));
        } else if (request.getUserId() != null) {
            user = utilisateurRepository.findById(request.getUserId())
                    .orElseThrow(() -> new BadCredentialsException("Utilisateur introuvable"));
        } else {
            throw new IllegalArgumentException("email ou userId requis");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getMotDePasse())) {
            throw new BadCredentialsException("Mot de passe actuel incorrect");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit contenir au moins 6 caractères");
        }

        user.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        user.setMustResetPassword(false);
        utilisateurRepository.save(user);

        // Revoke all refresh tokens (force re-login everywhere)
        refreshTokenService.revokeAllForUser(user.getId());

        // Notify by email
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getPrenom());
    }

    public UtilisateurDTO toDTO(Utilisateur u) {
        return UtilisateurDTO.builder()
                .id(u.getId()).nom(u.getNom()).prenom(u.getPrenom())
                .email(u.getEmail()).telephone(u.getTelephone())
                .role(u.getRole()).statut(u.getStatut())
                .dateCreation(u.getDateCreation())
                .mustResetPassword(u.getMustResetPassword())
                .build();
    }
}
