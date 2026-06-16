package ma.ferme.fermeintelligente.db;

import ma.ferme.fermeintelligente.entity.Utilisateur;
import ma.ferme.fermeintelligente.enums.Role;
import ma.ferme.fermeintelligente.repository.ParcelleRepository;
import ma.ferme.fermeintelligente.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Database test: verifies the Flyway migrations actually ran against a real
 * Postgres instance and produced the expected seed data — catches migration
 * regressions that unit tests (mocked repositories) can't see.
 */
@SpringBootTest
class FlywaySeedDataIT {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ParcelleRepository parcelleRepository;

    @Test
    void seededManagerExists_withCorrectRole() {
        Optional<Utilisateur> karim = utilisateurRepository.findByEmail("karim@ferme.ma");

        assertThat(karim).isPresent();
        assertThat(karim.get().getRole()).isEqualTo(Role.GESTIONNAIRE);
        assertThat(karim.get().getStatut()).isEqualTo("ACTIF");
    }

    @Test
    void seededParcellesExist() {
        assertThat(parcelleRepository.findAll()).isNotEmpty();
    }
}
