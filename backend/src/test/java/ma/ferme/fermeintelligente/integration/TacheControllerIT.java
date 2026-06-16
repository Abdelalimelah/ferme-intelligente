package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.dto.TacheDTO;
import ma.ferme.fermeintelligente.dto.UtilisateurDTO;
import ma.ferme.fermeintelligente.enums.Priorite;
import ma.ferme.fermeintelligente.enums.Role;
import ma.ferme.fermeintelligente.enums.StatutTache;
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
class TacheControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;
    private Long agriculteurId;
    private Long gestionnaireId;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeEach
    void login() {
        token = TestAuth.loginAndGetToken(restTemplate, url(""), "karim@ferme.ma");
        agriculteurId = findFirstByRole(Role.AGRICULTEUR);
        gestionnaireId = findFirstByRole(Role.GESTIONNAIRE);
    }

    private Long findFirstByRole(Role role) {
        UtilisateurDTO[] users = restTemplate.exchange(
                url("/api/utilisateurs/role/" + role), HttpMethod.GET,
                TestAuth.authEntity(token), UtilisateurDTO[].class).getBody();
        return users[0].getId();
    }

    @Test
    void getAll_returnsSeededTaches() {
        ResponseEntity<TacheDTO[]> response = restTemplate.exchange(
                url("/api/taches"), HttpMethod.GET, TestAuth.authEntity(token), TacheDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void getByAgriculteur_filtersCorrectly() {
        ResponseEntity<TacheDTO[]> response = restTemplate.exchange(
                url("/api/taches/agriculteur/" + agriculteurId), HttpMethod.GET,
                TestAuth.authEntity(token), TacheDTO[].class);

        assertThat(response.getBody()).allSatisfy(t -> assertThat(t.getAgriculteurId()).isEqualTo(agriculteurId));
    }

    @Test
    void create_thenDemarrerThenTerminer_followsLifecycle() {
        TacheDTO toCreate = TacheDTO.builder()
                .titre("Tâche test IT").priorite(Priorite.MOYENNE)
                .agriculteurId(agriculteurId).gestionnaireId(gestionnaireId).build();

        ResponseEntity<TacheDTO> created = restTemplate.exchange(
                url("/api/taches"), HttpMethod.POST, TestAuth.authEntity(token, toCreate), TacheDTO.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long id = created.getBody().getId();

        ResponseEntity<TacheDTO> started = restTemplate.exchange(
                url("/api/taches/" + id + "/demarrer"), HttpMethod.PUT, TestAuth.authEntity(token), TacheDTO.class);
        assertThat(started.getBody().getStatut()).isEqualTo(StatutTache.EN_COURS);

        ResponseEntity<TacheDTO> finished = restTemplate.exchange(
                url("/api/taches/" + id + "/terminer"), HttpMethod.PUT, TestAuth.authEntity(token), TacheDTO.class);
        assertThat(finished.getBody().getStatut()).isEqualTo(StatutTache.TERMINEE);
        assertThat(finished.getBody().getDateTerminee()).isNotNull();
    }

    @Test
    void create_withoutGestionnaire_isRejected() {
        TacheDTO dto = TacheDTO.builder().titre("Sans gestionnaire").agriculteurId(agriculteurId).build();

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/taches"), HttpMethod.POST, TestAuth.authEntity(token, dto), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
