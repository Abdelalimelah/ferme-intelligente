package ma.ferme.fermeintelligente.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Le refresh token est requis")
    private String refreshToken;
}
