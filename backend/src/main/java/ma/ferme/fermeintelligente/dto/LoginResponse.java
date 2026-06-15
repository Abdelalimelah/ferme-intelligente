package ma.ferme.fermeintelligente.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;           // short-lived access JWT
    private String refreshToken;    // long-lived refresh token
    private UtilisateurDTO user;

    /** Convenience constructor for backward compat */
    public LoginResponse(String token, UtilisateurDTO user) {
        this.token = token;
        this.user  = user;
    }
}
