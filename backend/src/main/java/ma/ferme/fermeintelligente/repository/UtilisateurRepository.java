package ma.ferme.fermeintelligente.repository;

import ma.ferme.fermeintelligente.entity.Utilisateur;
import ma.ferme.fermeintelligente.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findByRole(Role role);
}
