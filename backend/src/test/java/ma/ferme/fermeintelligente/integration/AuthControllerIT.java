package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.dto.LoginRequest;
import ma.ferme.fermeintelligente.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack integration test: real Spring context, real Postgres (Flyway
 * migrated + seeded via V1), real HTTP call through the controller layer.
 * Runs against the "ferme_test" database spun up as a service container in CI.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void login_withSeededManagerCredentials_returnsJwt() {
        LoginRequest request = new LoginRequest();
        request.setEmail("karim@ferme.ma");
        request.setMotDePasse("password123");

        ResponseEntity<LoginResponse> response =
                restTemplate.postForEntity(url("/api/auth/login"), request, LoginResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
    }

    @Test
    void login_withWrongPassword_isRejected() {
        LoginRequest request = new LoginRequest();
        request.setEmail("karim@ferme.ma");
        request.setMotDePasse("wrong-password");

        ResponseEntity<String> response =
                restTemplate.postForEntity(url("/api/auth/login"), request, String.class);

        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.OK);
    }
}
