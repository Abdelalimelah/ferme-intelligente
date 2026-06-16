package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.dto.FermeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FermeControllerIT {

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
        token = TestAuth.loginAndGetToken(restTemplate, url(""), "ahmed@ferme.ma");
    }

    @Test
    void getAll_returnsSeededFerme() {
        ResponseEntity<FermeDTO[]> response = restTemplate.exchange(
                url("/api/fermes"), org.springframework.http.HttpMethod.GET,
                TestAuth.authEntity(token), FermeDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()[0].getNom()).isEqualTo("Ferme Al-Baraka");
    }

    @Test
    void getByProprietaire_returnsOnlyTheirFermes() {
        ResponseEntity<FermeDTO[]> all = restTemplate.exchange(
                url("/api/fermes"), org.springframework.http.HttpMethod.GET,
                TestAuth.authEntity(token), FermeDTO[].class);
        Long proprietaireId = all.getBody()[0].getProprietaireId();

        ResponseEntity<FermeDTO[]> response = restTemplate.exchange(
                url("/api/fermes/proprietaire/" + proprietaireId), org.springframework.http.HttpMethod.GET,
                TestAuth.authEntity(token), FermeDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).allSatisfy(f -> assertThat(f.getProprietaireId()).isEqualTo(proprietaireId));
    }

    @Test
    void getById_unknownId_returns404() {
        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/fermes/999999"), org.springframework.http.HttpMethod.GET,
                TestAuth.authEntity(token), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void withoutToken_isRejected() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/api/fermes"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void create_withBlankNom_isRejected() {
        FermeDTO dto = FermeDTO.builder().nom("").proprietaireId(1L).build();

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/fermes"), org.springframework.http.HttpMethod.POST,
                TestAuth.authEntity(token, dto), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
