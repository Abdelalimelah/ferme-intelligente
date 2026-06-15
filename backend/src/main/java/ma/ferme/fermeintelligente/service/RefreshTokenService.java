package ma.ferme.fermeintelligente.service;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.entity.RefreshToken;
import ma.ferme.fermeintelligente.entity.Utilisateur;
import ma.ferme.fermeintelligente.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration:604800000}") // 7 days in ms
    private long refreshExpiration;

    /** Create a new refresh token for the given user, revoking all previous ones. */
    @Transactional
    public String createRefreshToken(Utilisateur utilisateur) {
        // Revoke all existing tokens for this user
        refreshTokenRepository.deleteByUtilisateurId(utilisateur.getId());

        String tokenValue = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        RefreshToken token = RefreshToken.builder()
                .token(tokenValue)
                .utilisateur(utilisateur)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .createdAt(LocalDateTime.now())
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(token);
        return tokenValue;
    }

    /** Validate the refresh token and return its Utilisateur. Throws on invalid/expired/revoked. */
    public Utilisateur validateAndGetUser(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BadCredentialsException("Refresh token invalide"));

        if (Boolean.TRUE.equals(token.getIsRevoked())) {
            throw new BadCredentialsException("Refresh token révoqué");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh token expiré — veuillez vous reconnecter");
        }
        return token.getUtilisateur();
    }

    /** Revoke a specific token (on logout). */
    @Transactional
    public void revokeToken(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue).ifPresent(t -> {
            t.setIsRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    /** Revoke all tokens for a user (on password change or forced logout). */
    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepository.deleteByUtilisateurId(userId);
    }

    /** Cleanup expired/revoked tokens — runs every night at 2am. */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now());
    }
}
