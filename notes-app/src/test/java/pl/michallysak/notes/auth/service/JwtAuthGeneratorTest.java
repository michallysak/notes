package pl.michallysak.notes.auth.service;

import org.junit.jupiter.api.Test;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.model.UserValue;
import java.util.Base64;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class JwtAuthGeneratorTest {
    @Test
    void generateToken_shouldReturnValidToken_whenFullConstructor() {
        // given
        JwtAuthGenerator generator = new JwtAuthGenerator(generateSecret(), 10000);
        // when
        AuthToken token = generator.generateToken(createUser());
        // then
        assertValidToken(token);
    }

    @Test
    void generateToken_shouldReturnValidToken_defaultConstructor() {
        // given
        JwtAuthGenerator generator = new JwtAuthGenerator();
        // when
        AuthToken token = generator.generateToken(createUser());
        // then
        assertValidToken(token);
    }

    private static String generateSecret() {
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < keyBytes.length; i++) {
            keyBytes[i] = (byte) i;
        }
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    private static UserValue createUser() {
        return new UserValue(UUID.randomUUID(), Email.of("user@example.com"));
    }

    private static void assertValidToken(AuthToken token) {
        assertNotNull(token);
        assertNotNull(token.token());
        assertNotNull(token.expiresAt());
    }

}
