package pl.michallysak.notes.application.quarkus.common;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonWebTokenProviderTest {

  @Mock JsonWebToken jwt;

  @InjectMocks JsonWebTokenProvider provider;

  @Test
  void getExpired_shouldReturnEmpty_whenJwtIsNull() {
    // given
    JsonWebTokenProvider nullProvider = new JsonWebTokenProvider(null);
    // when
    Optional<Instant> result = nullProvider.getExpired();
    // then
    assertTrue(result.isEmpty());
  }

  @Test
  void getExpired_shouldReturnEmpty_whenExpClaimIsNull() {
    // given
    when(jwt.getClaim("exp")).thenReturn(null);
    // when
    Optional<Instant> result = provider.getExpired();
    // then
    assertTrue(result.isEmpty());
  }

  @Test
  void getExpired_shouldReturnEmpty_whenExpClaimIsNotNumber() {
    // given
    when(jwt.getClaim("exp")).thenReturn("not-a-number");
    // when
    Optional<Instant> result = provider.getExpired();
    // then
    assertTrue(result.isEmpty());
  }

  @Test
  void getExpired_shouldReturnEmpty_whenExpClaimIsInPast() {
    // given
    long pastEpochSecond = Instant.now().minusSeconds(3600).getEpochSecond();
    when(jwt.getClaim("exp")).thenReturn(pastEpochSecond);
    // when
    Optional<Instant> result = provider.getExpired();
    // then
    assertTrue(result.isEmpty());
  }

  @Test
  void getExpired_shouldReturnInstant_whenExpClaimIsInFuture() {
    // given
    long futureEpochSecond = Instant.now().plusSeconds(3600).getEpochSecond();
    when(jwt.getClaim("exp")).thenReturn(futureEpochSecond);
    // when
    Optional<Instant> result = provider.getExpired();
    // then
    assertTrue(result.isPresent());
    assertEquals(Instant.ofEpochSecond(futureEpochSecond), result.get());
  }

  @Test
  void getExpired_shouldReturnInstant_whenExpClaimIsInteger() {
    // given
    int futureEpochSecond = (int) (Instant.now().plusSeconds(3600).getEpochSecond());
    when(jwt.getClaim("exp")).thenReturn(futureEpochSecond);
    // when
    Optional<Instant> result = provider.getExpired();
    // then
    assertTrue(result.isPresent());
    assertEquals(Instant.ofEpochSecond(futureEpochSecond), result.get());
  }
}
