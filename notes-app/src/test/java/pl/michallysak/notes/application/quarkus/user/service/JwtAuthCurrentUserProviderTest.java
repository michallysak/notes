package pl.michallysak.notes.application.quarkus.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.auth.exception.AuthException;

class JwtAuthCurrentUserProviderTest {
  @Test
  void getCurrentUserId_shouldReturnUuidFromJwtSubject() {
    // given
    JsonWebToken jwt = mock(JsonWebToken.class);
    UUID uuid = UUID.randomUUID();
    when(jwt.getSubject()).thenReturn(uuid.toString());
    // when
    JwtAuthCurrentUserProvider provider = new JwtAuthCurrentUserProvider(jwt);
    // then
    assertEquals(uuid, provider.getCurrentUserId());
  }

  @Test
  void getCurrentUserId_shouldThrowAuthExceptionOnInvalidSubject() {
    // given
    JsonWebToken jwt = mock(JsonWebToken.class);
    when(jwt.getSubject()).thenReturn("not-a-uuid");
    JwtAuthCurrentUserProvider provider = new JwtAuthCurrentUserProvider(jwt);
    // when
    AuthException exception = assertThrows(AuthException.class, provider::getCurrentUserId);
    // then
    assertTrue(exception.getMessage().contains("Not authenticated"));
  }

  @Test
  void getCurrentUserId_shouldReturnNullIfJwtIsNull() {
    // when
    JwtAuthCurrentUserProvider provider = new JwtAuthCurrentUserProvider(null);
    // then
    assertNull(provider.getCurrentUserId());
  }
}
