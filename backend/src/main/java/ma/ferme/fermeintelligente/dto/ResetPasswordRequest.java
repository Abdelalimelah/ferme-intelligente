package ma.ferme.fermeintelligente.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class ResetPasswordRequest {
    private String email;
    private Long userId;
    private String oldPassword;   // temporary password or current password
    private String newPassword;
}
