package ma.ferme.fermeintelligente.integration;

import ma.ferme.fermeintelligente.dto.LoginRequest;
import ma.ferme.fermeintelligente.dto.LoginResponse;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

/** Shared helper for integration tests: logs in as a seeded user and builds Bearer-auth headers. */
final class TestAuth {

    private TestAuth() {}

    static String loginAndGetToken(TestRestTemplate restTemplate, String baseUrl, String email) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setMotDePasse("password123");

        LoginResponse response = restTemplate.postForObject(
                baseUrl + "/api/auth/login", request, LoginResponse.class);
        return response.getToken();
    }

    static HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    static <T> HttpEntity<T> authEntity(String token, T body) {
        return new HttpEntity<>(body, authHeaders(token));
    }

    static HttpEntity<Void> authEntity(String token) {
        return new HttpEntity<>(null, authHeaders(token));
    }
}
