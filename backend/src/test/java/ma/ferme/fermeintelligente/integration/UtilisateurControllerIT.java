package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.dto.UtilisateurDTO;
import ma.ferme.fermeintelligente.enums.Role;
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
class UtilisateurControllerIT {

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
    void getAll_neverLeaksRawPassword() {
        ResponseEntity<UtilisateurDTO[]> response = restTemplate.exchange(
                url("/api/utilisateurs"), HttpMethod.GET, TestAuth.authEntity(token), UtilisateurDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody()).allSatisfy(u -> assertThat(u.getMotDePasse()).isNull());
    }

    @Test
    void getByRole_returnsOnlyMatchingRole() {
        ResponseEntity<UtilisateurDTO[]> response = restTemplate.exchange(
                url("/api/utilisateurs/role/AGRICULTEUR"), HttpMethod.GET,
                TestAuth.authEntity(token), UtilisateurDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).allSatisfy(u -> assertThat(u.getRole()).isEqualTo(Role.AGRICULTEUR));
    }

    @Test
    void create_withInvalidEmail_isRejected() {
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .nom("Test").prenom("User").email("not-an-email").role(Role.AGRICULTEUR).build();

        ResponseEntity<String> response = restTemplate.exchange(
                url("/api/utilisateurs"), HttpMethod.POST, TestAuth.authEntity(token, dto), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void create_returnsTemporaryPasswordOnce() {
        UtilisateurDTO dto = UtilisateurDTO.builder()
                .nom("Nouveau").prenom("Worker").email("nouveau.worker." + System.nanoTime() + "@ferme.ma")
                .role(Role.AGRICULTEUR).build();

        ResponseEntity<UtilisateurDTO> response = restTemplate.exchange(
                url("/api/utilisateurs"), HttpMethod.POST, TestAuth.authEntity(token, dto), UtilisateurDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTemporaryPassword()).isNotBlank();
        assertThat(response.getBody().getMotDePasse()).isNull();
    }
}
