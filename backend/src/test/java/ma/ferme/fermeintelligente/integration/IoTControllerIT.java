package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.dto.CapteurDTO;
import ma.ferme.fermeintelligente.dto.IoTDataRequest;
import ma.ferme.fermeintelligente.dto.IoTDataResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IoT ingestion endpoints are deliberately public (no JWT) — Arduino/sensor
 * devices can't do an OAuth-style login. See IoTController javadoc.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IoTControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private CapteurDTO firstTemperatureCapteur() {
        String token = TestAuth.loginAndGetToken(restTemplate, url(""), "karim@ferme.ma");
        CapteurDTO[] capteurs = restTemplate.exchange(
                url("/api/capteurs"), HttpMethod.GET, TestAuth.authEntity(token), CapteurDTO[].class).getBody();
        return java.util.Arrays.stream(capteurs)
                .filter(c -> "Température".equals(c.getType())).findFirst().orElseThrow();
    }

    @Test
    void ingestData_byCapteurId_storesReadingWithoutAuth() {
        CapteurDTO capteur = firstTemperatureCapteur();

        IoTDataRequest request = IoTDataRequest.builder()
                .capteurId(capteur.getId()).valeur(22.0).unite("°C").build();

        ResponseEntity<IoTDataResponse> response = restTemplate.postForEntity(
                url("/api/iot/data"), request, IoTDataResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCapteurId()).isEqualTo(capteur.getId());
        assertThat(response.getBody().isAlerteGeneree()).isFalse();
    }

    @Test
    void ingestData_outOfThreshold_generatesAlert() {
        CapteurDTO capteur = firstTemperatureCapteur();

        IoTDataRequest request = IoTDataRequest.builder()
                .capteurId(capteur.getId()).valeur(99.0).unite("°C").build();

        ResponseEntity<IoTDataResponse> response = restTemplate.postForEntity(
                url("/api/iot/data"), request, IoTDataResponse.class);

        assertThat(response.getBody().isAlerteGeneree()).isTrue();
        assertThat(response.getBody().getAlerteMessage()).isNotBlank();
    }

    @Test
    void ingestData_byParcelleAndType_resolvesCapteur() {
        CapteurDTO capteur = firstTemperatureCapteur();

        IoTDataRequest request = IoTDataRequest.builder()
                .parcelleId(capteur.getParcelleId()).type("Température").valeur(21.0).build();

        ResponseEntity<IoTDataResponse> response = restTemplate.postForEntity(
                url("/api/iot/data"), request, IoTDataResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getParcelleId()).isEqualTo(capteur.getParcelleId());
    }

    @Test
    void ingestBatch_processesAllReadings() {
        CapteurDTO capteur = firstTemperatureCapteur();
        List<IoTDataRequest> batch = List.of(
                IoTDataRequest.builder().capteurId(capteur.getId()).valeur(20.0).build(),
                IoTDataRequest.builder().capteurId(capteur.getId()).valeur(21.0).build());

        ResponseEntity<IoTDataResponse[]> response = restTemplate.postForEntity(
                url("/api/iot/data/batch"), batch, IoTDataResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void ingestData_withUnknownCapteur_returns404() {
        IoTDataRequest request = IoTDataRequest.builder().capteurId(999999L).valeur(10.0).build();

        ResponseEntity<String> response = restTemplate.postForEntity(url("/api/iot/data"), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
