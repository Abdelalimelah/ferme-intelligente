package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.dto.DroneSimulationStatusDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DroneSimulationControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeEach
    void login() {
        token = TestAuth.loginAndGetToken(restTemplate, url(""), "karim@ferme.ma");
    }

    @Test
    void status_defaultsToDisabled() {
        ResponseEntity<DroneSimulationStatusDTO> response = restTemplate.exchange(
                url("/api/simulation/drone/status"), HttpMethod.GET,
                TestAuth.authEntity(token), DroneSimulationStatusDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getIntervalSeconds()).isGreaterThan(0);
    }

    @Test
    void toggle_flipsEnabledState_andIsRestoredAfterTest() {
        boolean before = restTemplate.exchange(
                url("/api/simulation/drone/status"), HttpMethod.GET,
                TestAuth.authEntity(token), DroneSimulationStatusDTO.class).getBody().isEnabled();

        DroneSimulationStatusDTO toggled = restTemplate.exchange(
                url("/api/simulation/drone/toggle"), HttpMethod.POST,
                TestAuth.authEntity(token), DroneSimulationStatusDTO.class).getBody();
        assertThat(toggled.isEnabled()).isEqualTo(!before);

        // Toggle back so this doesn't leak a running simulation into other tests
        // sharing the same cached Spring context.
        DroneSimulationStatusDTO restored = restTemplate.exchange(
                url("/api/simulation/drone/toggle"), HttpMethod.POST,
                TestAuth.authEntity(token), DroneSimulationStatusDTO.class).getBody();
        assertThat(restored.isEnabled()).isEqualTo(before);
    }
}
