package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.dto.ParcelleDTO;
import ma.ferme.fermeintelligente.entity.ImageParcelle;
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
class ImageParcelleControllerIT {

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
    void getByParcelle_returnsEmptyListWhenNoImagesYet() {
        ParcelleDTO[] parcelles = restTemplate.exchange(
                url("/api/parcelles"), HttpMethod.GET, TestAuth.authEntity(token), ParcelleDTO[].class).getBody();

        ResponseEntity<ImageParcelle[]> response = restTemplate.exchange(
                url("/api/images/parcelle/" + parcelles[0].getId()), HttpMethod.GET,
                TestAuth.authEntity(token), ImageParcelle[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getByParcelle_unknownParcelle_returnsEmptyList() {
        ResponseEntity<ImageParcelle[]> response = restTemplate.exchange(
                url("/api/images/parcelle/999999"), HttpMethod.GET,
                TestAuth.authEntity(token), ImageParcelle[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}
