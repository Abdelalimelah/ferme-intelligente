package ma.ferme.fermeintelligente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync        // for EmailService.@Async methods
@EnableScheduling   // for RefreshTokenService.@Scheduled cleanup
public class FermeIntelligentApplication {
    public static void main(String[] args) {
        SpringApplication.run(FermeIntelligentApplication.class, args);
    }
}
