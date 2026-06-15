package ma.ferme.fermeintelligente.config;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String encoded = passwordEncoder.encode("password123");
        utilisateurRepository.findAll().forEach(user -> {
            if (!user.getMotDePasse().startsWith("$2a$10$") ||
                    user.getMotDePasse().length() < 50) {
                user.setMotDePasse(encoded);
                utilisateurRepository.save(user);
            }
        });
    }
}
