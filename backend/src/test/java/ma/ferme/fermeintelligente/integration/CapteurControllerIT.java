package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.dto.CapteurDTO;
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
class CapteurControllerIT {

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
    void getAll_returnsSeededCapteurs() {
        ResponseEntity<CapteurDTO[]> response = restTemplate.exchange(
                url("/api/capteurs"), HttpMethod.GET, TestAuth.authEntity(token), CapteurDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(CapteurDTO::getType)
                .contains("Température", "Humidité", "pH");
    }

    @Test
    void getByParcelle_filtersCorrectly() {
        CapteurDTO[] all = restTemplate.exchange(
                url("/api/capteurs"), HttpMethod.GET, TestAuth.authEntity(token), CapteurDTO[].class).getBody();
        Long parcelleId = all[0].getParcelleId();

        ResponseEntity<CapteurDTO[]> response = restTemplate.exchange(
                url("/api/capteurs/parcelle/" + parcelleId), HttpMethod.GET,
                TestAuth.authEntity(token), CapteurDTO[].class);

        assertThat(response.getBody()).allSatisfy(c -> assertThat(c.getParcelleId()).isEqualTo(parcelleId));
    }

    @Test
    void create_withoutParcelleId_isRejected() {
        CapteurDTO dto = CapteurDTO.builder().type("Luminosité").build();

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/capteurs"), HttpMethod.POST, TestAuth.authEntity(token, dto), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createThenDelete_roundTrip() {
        CapteurDTO[] all = restTemplate.exchange(
                url("/api/capteurs"), HttpMethod.GET, TestAuth.authEntity(token), CapteurDTO[].class).getBody();
        Long parcelleId = all[0].getParcelleId();

        CapteurDTO toCreate = CapteurDTO.builder().type("Luminosité").unite("lux").parcelleId(parcelleId).build();
        ResponseEntity<CapteurDTO> created = restTemplate.exchange(
                url("/api/capteurs"), HttpMethod.POST, TestAuth.authEntity(token, toCreate), CapteurDTO.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(created.getBody().getId()).isNotNull();

        restTemplate.exchange(url("/api/capteurs/" + created.getBody().getId()), HttpMethod.DELETE,
                TestAuth.authEntity(token), Void.class);

        ResponseEntity<String> afterDelete = restTemplate.exchange(
                url("/api/capteurs/" + created.getBody().getId()), HttpMethod.GET,
                TestAuth.authEntity(token), String.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
