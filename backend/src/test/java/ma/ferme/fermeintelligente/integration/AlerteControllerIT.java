package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.dto.AlerteDTO;
import ma.ferme.fermeintelligente.dto.ParcelleDTO;
import ma.ferme.fermeintelligente.enums.NiveauAlerte;
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
class AlerteControllerIT {

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
    void getAll_returnsSeededAlerts() {
        ResponseEntity<AlerteDTO[]> response = restTemplate.exchange(
                url("/api/alertes"), HttpMethod.GET, TestAuth.authEntity(token), AlerteDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void getUnread_onlyReturnsUnreadAlerts() {
        ResponseEntity<AlerteDTO[]> response = restTemplate.exchange(
                url("/api/alertes/unread"), HttpMethod.GET, TestAuth.authEntity(token), AlerteDTO[].class);

        assertThat(response.getBody()).allSatisfy(a -> assertThat(a.getEstLue()).isFalse());
    }

    @Test
    void markAsRead_thenCountUnread_decreases() {
        AlerteDTO[] unreadBefore = restTemplate.exchange(
                url("/api/alertes/unread"), HttpMethod.GET, TestAuth.authEntity(token), AlerteDTO[].class).getBody();
        Long countBefore = restTemplate.exchange(
                url("/api/alertes/unread/count"), HttpMethod.GET, TestAuth.authEntity(token), Long.class).getBody();
        Long alerteId = unreadBefore[0].getId();

        restTemplate.exchange(url("/api/alertes/" + alerteId + "/read"), HttpMethod.PUT,
                TestAuth.authEntity(token), Void.class);

        Long countAfter = restTemplate.exchange(
                url("/api/alertes/unread/count"), HttpMethod.GET, TestAuth.authEntity(token), Long.class).getBody();
        assertThat(countAfter).isEqualTo(countBefore - 1);
    }

    @Test
    void create_persistsAndIsRetrievable() {
        ParcelleDTO[] parcelles = restTemplate.exchange(
                url("/api/parcelles"), HttpMethod.GET, TestAuth.authEntity(token), ParcelleDTO[].class).getBody();

        AlerteDTO toCreate = AlerteDTO.builder()
                .type("Test").message("Alerte de test IT").niveau(NiveauAlerte.INFO)
                .parcelleId(parcelles[0].getId()).build();

        ResponseEntity<AlerteDTO> created = restTemplate.exchange(
                url("/api/alertes"), HttpMethod.POST, TestAuth.authEntity(token, toCreate), AlerteDTO.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(created.getBody().getId()).isNotNull();
    }
}
