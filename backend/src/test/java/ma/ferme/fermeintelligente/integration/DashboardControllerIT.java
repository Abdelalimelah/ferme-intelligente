package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.dto.DashboardStats;
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
class DashboardControllerIT {

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

    private Long findFirstByRole(Role role) {
        UtilisateurDTO[] users = restTemplate.exchange(
                url("/api/utilisateurs/role/" + role), HttpMethod.GET,
                TestAuth.authEntity(token), UtilisateurDTO[].class).getBody();
        return users[0].getId();
    }

    @Test
    void ownerStats_reflectsSeededData() {
        Long ownerId = findFirstByRole(Role.PROPRIETAIRE);

        ResponseEntity<DashboardStats.OwnerStats> response = restTemplate.exchange(
                url("/api/dashboard/owner/" + ownerId), HttpMethod.GET,
                TestAuth.authEntity(token), DashboardStats.OwnerStats.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalFermes()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void managerStats_includesSensorAverages() {
        Long managerId = findFirstByRole(Role.GESTIONNAIRE);

        ResponseEntity<DashboardStats.ManagerStats> response = restTemplate.exchange(
                url("/api/dashboard/manager/" + managerId), HttpMethod.GET,
                TestAuth.authEntity(token), DashboardStats.ManagerStats.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTotalParcelles()).isGreaterThanOrEqualTo(4);
    }

    @Test
    void workerStats_countsTasksByStatus() {
        Long workerId = findFirstByRole(Role.AGRICULTEUR);

        ResponseEntity<DashboardStats.WorkerStats> response = restTemplate.exchange(
                url("/api/dashboard/worker/" + workerId), HttpMethod.GET,
                TestAuth.authEntity(token), DashboardStats.WorkerStats.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
