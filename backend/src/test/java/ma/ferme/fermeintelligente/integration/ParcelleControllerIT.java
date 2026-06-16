package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.dto.ParcelleDTO;
import ma.ferme.fermeintelligente.dto.ParcelleDetailDTO;
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
class ParcelleControllerIT {

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
    void getAll_returnsSeededParcelles() {
        ResponseEntity<ParcelleDTO[]> response = restTemplate.exchange(
                url("/api/parcelles"), HttpMethod.GET, TestAuth.authEntity(token), ParcelleDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).extracting(ParcelleDTO::getNom)
                .contains("Parcelle Nord", "Parcelle Sud", "Parcelle Est", "Parcelle Ouest");
    }

    @Test
    void getByFerme_filtersCorrectly() {
        ParcelleDTO[] all = restTemplate.exchange(
                url("/api/parcelles"), HttpMethod.GET, TestAuth.authEntity(token), ParcelleDTO[].class).getBody();
        Long fermeId = all[0].getFermeId();

        ResponseEntity<ParcelleDTO[]> response = restTemplate.exchange(
                url("/api/parcelles/ferme/" + fermeId), HttpMethod.GET,
                TestAuth.authEntity(token), ParcelleDTO[].class);

        assertThat(response.getBody()).allSatisfy(p -> assertThat(p.getFermeId()).isEqualTo(fermeId));
    }

    @Test
    void getDetail_returnsLiveSensorsAndAlerts() {
        ParcelleDTO[] all = restTemplate.exchange(
                url("/api/parcelles"), HttpMethod.GET, TestAuth.authEntity(token), ParcelleDTO[].class).getBody();
        Long parcelleId = all[0].getId();

        ResponseEntity<ParcelleDetailDTO> response = restTemplate.exchange(
                url("/api/parcelles/" + parcelleId + "/detail"), HttpMethod.GET,
                TestAuth.authEntity(token), ParcelleDetailDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void create_withoutFermeId_isRejected() {
        ParcelleDTO dto = ParcelleDTO.builder().nom("Test Parcelle").surface(5.0).build();

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/parcelles"), HttpMethod.POST, TestAuth.authEntity(token, dto), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createUpdateDelete_roundTrip() {
        ParcelleDTO[] fermes = restTemplate.exchange(
                url("/api/parcelles"), HttpMethod.GET, TestAuth.authEntity(token), ParcelleDTO[].class).getBody();
        Long fermeId = fermes[0].getFermeId();

        ParcelleDTO toCreate = ParcelleDTO.builder()
                .nom("Parcelle Test IT").surface(3.5).typeCulture("Carottes").fermeId(fermeId).build();
        ResponseEntity<ParcelleDTO> created = restTemplate.exchange(
                url("/api/parcelles"), HttpMethod.POST, TestAuth.authEntity(token, toCreate), ParcelleDTO.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long id = created.getBody().getId();

        created.getBody().setSurface(7.0);
        ResponseEntity<ParcelleDTO> updated = restTemplate.exchange(
                url("/api/parcelles/" + id), HttpMethod.PUT, TestAuth.authEntity(token, created.getBody()), ParcelleDTO.class);
        assertThat(updated.getBody().getSurface()).isEqualTo(7.0);

        restTemplate.exchange(url("/api/parcelles/" + id), HttpMethod.DELETE, TestAuth.authEntity(token), Void.class);

        ResponseEntity<String> afterDelete = restTemplate.exchange(
                url("/api/parcelles/" + id), HttpMethod.GET, TestAuth.authEntity(token), String.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
