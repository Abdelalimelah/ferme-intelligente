package ma.ferme.fermeintelligente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.ferme.fermeintelligente.enums.Role;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UtilisateurDTO {
    private Long id;

    @NotBlank(message = "Le nom est requis")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est requis")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String prenom;

    @NotBlank(message = "L'email est requis")
    @Email(message = "L'email n'est pas valide")
    private String email;

    @Size(max = 20, message = "Le téléphone ne peut dépasser 20 caractères")
    private String telephone;

    private Role role;
    private String statut;
    private LocalDateTime dateCreation;
    private String motDePasse;
    private Boolean mustResetPassword;

    // One-time field: returned only on creation
    private String temporaryPassword;
}
