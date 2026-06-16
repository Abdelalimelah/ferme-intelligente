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

/**
 * Scoped to the pure-DB-read endpoint only. The /analyze* endpoints call out
 * to the AI microservice (or a dataset on disk) and are already covered by
 * AIClassificationServiceTest's mocked unit tests — exercising them here
 * would make this IT test depend on external process availability.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AIControllerIT {

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
    void getDiseasesByParcelle_returnsEmptyListWhenNoAnalysisYet() {
        ParcelleDTO[] parcelles = restTemplate.exchange(
                url("/api/parcelles"), HttpMethod.GET, TestAuth.authEntity(token), ParcelleDTO[].class).getBody();

        ResponseEntity<ParcelleDetailDTO.DiseaseResultDTO[]> response = restTemplate.exchange(
                url("/api/ai/diseases/parcelle/" + parcelles[0].getId()), HttpMethod.GET,
                TestAuth.authEntity(token), ParcelleDetailDTO.DiseaseResultDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void withoutToken_isRejected() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/api/ai/diseases/parcelle/1"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
