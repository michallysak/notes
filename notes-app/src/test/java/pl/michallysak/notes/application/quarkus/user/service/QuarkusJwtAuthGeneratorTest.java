package pl.michallysak.notes.application.quarkus.user.service;

import org.junit.jupiter.api.Test;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.user.model.UserValue;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuarkusJwtAuthGeneratorTest {
    @Test
    void generateToken_shouldReturnValidAuthToken() {
        // given
        QuarkusJwtAuthGenerator generator = new QuarkusJwtAuthGenerator();
        UserValue user = mock(UserValue.class);
        when(user.id()).thenReturn(java.util.UUID.randomUUID());
        // when
        AuthToken token = generator.generateToken(user);
        // then
        assertNotNull(token.token());
        assertNotNull(token.expiresAt());
        assertTrue(token.expiresAt().isAfter(OffsetDateTime.now()));
        assertTrue(Duration.between(OffsetDateTime.now(), token.expiresAt()).toHours() <= 1);
    }
}

