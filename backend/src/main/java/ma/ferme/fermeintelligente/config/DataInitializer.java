package ma.ferme.fermeintelligente.config;

import lombok.RequiredArgsConstructor;
import ma.ferme.fermeintelligente.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Seeds/repairs demo account passwords — never runs in prod, where a
// silently-applied default password would be a real vulnerability.
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.demo-password:password123}")
    private String demoPassword;

    @Override
    public void run(String... args) {
        String encoded = passwordEncoder.encode(demoPassword);
        utilisateurRepository.findAll().forEach(user -> {
            if (!user.getMotDePasse().startsWith("$2a$10$") ||
                    user.getMotDePasse().length() < 50) {
                user.setMotDePasse(encoded);
                utilisateurRepository.save(user);
            }
        });
    }
}
