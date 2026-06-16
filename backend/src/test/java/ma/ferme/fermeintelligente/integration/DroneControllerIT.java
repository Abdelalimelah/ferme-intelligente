package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.entity.Drone;
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
class DroneControllerIT {

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
    void getAll_returnsSeededDrone() {
        ResponseEntity<Drone[]> response = restTemplate.exchange(
                url("/api/drones"), HttpMethod.GET, TestAuth.authEntity(token), Drone[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(Drone::getModele).contains("DJI Agras T40");
    }
}
