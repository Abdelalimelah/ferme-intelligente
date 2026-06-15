package ma.ferme.fermeintelligente.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshTokenResponse {
    private String token;          // new access token
    private String refreshToken;   // new refresh token (rotated)
}
