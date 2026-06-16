package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.dto.RapportDTO;
import ma.ferme.fermeintelligente.dto.UtilisateurDTO;
import ma.ferme.fermeintelligente.enums.Role;
import ma.ferme.fermeintelligente.enums.StatutRapport;
import ma.ferme.fermeintelligente.enums.TypeRapport;
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
class RapportControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;
    private Long auteurId;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeEach
    void login() {
        token = TestAuth.loginAndGetToken(restTemplate, url(""), "karim@ferme.ma");
        UtilisateurDTO[] gestionnaires = restTemplate.exchange(
                url("/api/utilisateurs/role/" + Role.GESTIONNAIRE), HttpMethod.GET,
                TestAuth.authEntity(token), UtilisateurDTO[].class).getBody();
        auteurId = gestionnaires[0].getId();
    }

    @Test
    void getAll_returnsSeededRapport() {
        ResponseEntity<RapportDTO[]> response = restTemplate.exchange(
                url("/api/rapports"), HttpMethod.GET, TestAuth.authEntity(token), RapportDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void getByType_filtersCorrectly() {
        ResponseEntity<RapportDTO[]> response = restTemplate.exchange(
                url("/api/rapports/type/" + TypeRapport.RAPPORT), HttpMethod.GET,
                TestAuth.authEntity(token), RapportDTO[].class);

        assertThat(response.getBody()).allSatisfy(r -> assertThat(r.getType()).isEqualTo(TypeRapport.RAPPORT));
    }

    @Test
    void create_thenUpdateStatut_followsLifecycle() {
        RapportDTO toCreate = RapportDTO.builder()
                .type(TypeRapport.PLAINTE).sujet("Sujet test IT").contenu("Contenu test IT")
                .auteurId(auteurId).build();

        ResponseEntity<RapportDTO> created = restTemplate.exchange(
                url("/api/rapports"), HttpMethod.POST, TestAuth.authEntity(token, toCreate), RapportDTO.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long id = created.getBody().getId();

        ResponseEntity<RapportDTO> updated = restTemplate.exchange(
                url("/api/rapports/" + id + "/statut?statut=" + StatutRapport.TRAITE), HttpMethod.PUT,
                TestAuth.authEntity(token), RapportDTO.class);

        assertThat(updated.getBody().getStatut()).isEqualTo(StatutRapport.TRAITE);
    }

    @Test
    void create_withBlankSujet_isRejected() {
        RapportDTO dto = RapportDTO.builder()
                .type(TypeRapport.RAPPORT).sujet("").contenu("x").auteurId(auteurId).build();

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/rapports"), HttpMethod.POST, TestAuth.authEntity(token, dto), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
