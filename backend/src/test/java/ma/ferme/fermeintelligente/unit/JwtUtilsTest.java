package ma.ferme.fermeintelligente.unit;

import ma.ferme.fermeintelligente.config.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit test — no Spring context, no database. Verifies JWT generation,
 * parsing, and rejection of invalid tokens in isolation.
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", "test-secret-key-must-be-at-least-32-characters-long");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 3600_000L);
    }

    @Test
    void generateToken_thenExtractEmail_roundTrips() {
        String token = jwtUtils.generateToken("karim@ferme.ma", "GESTIONNAIRE");

        assertThat(token).isNotBlank();
        assertThat(jwtUtils.getEmailFromToken(token)).isEqualTo("karim@ferme.ma");
        assertThat(jwtUtils.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_rejectsGarbage() {
        assertThat(jwtUtils.validateToken("not-a-real-jwt")).isFalse();
    }

    @Test
    void validateToken_rejectsTokenSignedWithDifferentSecret() {
        JwtUtils otherSigner = new JwtUtils();
        ReflectionTestUtils.setField(otherSigner, "secret", "a-completely-different-secret-key-32-chars-min");
        ReflectionTestUtils.setField(otherSigner, "expiration", 3600_000L);

        String tokenFromOtherSecret = otherSigner.generateToken("intrus@ferme.ma", "PROPRIETAIRE");

        assertThat(jwtUtils.validateToken(tokenFromOtherSecret)).isFalse();
    }

    @Test
    void validateToken_rejectsExpiredToken() {
        ReflectionTestUtils.setField(jwtUtils, "expiration", -1000L); // already expired
        String expiredToken = jwtUtils.generateToken("karim@ferme.ma", "GESTIONNAIRE");

        assertThat(jwtUtils.validateToken(expiredToken)).isFalse();
    }
}
