package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.dto.CapteurDTO;
import ma.ferme.fermeintelligente.dto.DonneeCapteurDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DonneeCapteurControllerIT {

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

    private Long firstCapteurId() {
        CapteurDTO[] capteurs = restTemplate.exchange(
                url("/api/capteurs"), HttpMethod.GET, TestAuth.authEntity(token), CapteurDTO[].class).getBody();
        return capteurs[0].getId();
    }

    @Test
    void create_thenGetByCapteur_returnsTheReading() {
        Long capteurId = firstCapteurId();

        DonneeCapteurDTO toCreate = DonneeCapteurDTO.builder()
                .valeur(23.5).unite("°C").dateReleve(LocalDateTime.now()).capteurId(capteurId).build();
        ResponseEntity<DonneeCapteurDTO> created = restTemplate.exchange(
                url("/api/donnees-capteur"), HttpMethod.POST, TestAuth.authEntity(token, toCreate), DonneeCapteurDTO.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<DonneeCapteurDTO[]> history = restTemplate.exchange(
                url("/api/donnees-capteur/capteur/" + capteurId), HttpMethod.GET,
                TestAuth.authEntity(token), DonneeCapteurDTO[].class);

        assertThat(history.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(history.getBody()).extracting(DonneeCapteurDTO::getValeur).contains(23.5);
    }

    @Test
    void getByDateRange_filtersOutOfRangeReadings() {
        Long capteurId = firstCapteurId();
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        ResponseEntity<DonneeCapteurDTO[]> response = restTemplate.exchange(
                url("/api/donnees-capteur/capteur/" + capteurId + "/range?start=" + start + "&end=" + end),
                HttpMethod.GET, TestAuth.authEntity(token), DonneeCapteurDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
