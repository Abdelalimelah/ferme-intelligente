package ma.ferme.fermeintelligente.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public OpenAPI fermeIntelligentOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Ferme Intelligente API")
                        .version("1.0.0")
                        .description("""
                                REST API for the Smart Farm Management System.

                                **Roles:**
                                - `PROPRIETAIRE` — owner, manages farms/users
                                - `GESTIONNAIRE` — manager, manages parcels/sensors/tasks
                                - `AGRICULTEUR` — worker, views assigned tasks/parcels

                                **Authentication:** Use `POST /api/auth/login` to get a JWT,
                                then click **Authorize** above and enter `Bearer <token>`.
                                """)
                        .contact(new Contact()
                                .name("Ferme Intelligente")
                                .email("contact@ferme-intelligente.ma"))
                        .license(new License().name("MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development"),
                        new Server().url("https://api.ferme-intelligente.ma").description("Production")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter: Bearer {your JWT token}")));
    }
}
