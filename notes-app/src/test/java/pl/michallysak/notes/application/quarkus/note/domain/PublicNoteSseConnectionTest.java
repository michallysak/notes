package pl.michallysak.notes.application.quarkus.note.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class PublicNoteSseConnectionTest {

  @Test
  void constructor_shouldCreatePublicConnection() {
    // given
    UUID publicShareId = UUID.randomUUID();
    // when
    PublicNoteSseConnection connection =
        new PublicNoteSseConnection(publicShareId, Set.of("EVENT"), getExpiresAt());
    // then
    assertEquals(publicShareId, connection.getPublicShareId());
    assertTrue(connection.isViewingPublicShare(publicShareId));
    assertFalse(connection.isViewingPublicShare(UUID.randomUUID()));
    assertNotNull(connection.getStreamKey());
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenPublicShareIdIsNull() {
    // when
    Executable executable =
        () -> new PublicNoteSseConnection(null, Set.of("EVENT"), getExpiresAt());
    // then
    assertThrows(NullPointerException.class, executable);
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenEventsIsNull() {
    // when
    Executable executable =
        () -> new PublicNoteSseConnection(UUID.randomUUID(), null, getExpiresAt());
    // then
    assertThrows(NullPointerException.class, executable);
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenExpiresAtIsNull() {
    // when
    Executable executable =
        () -> new PublicNoteSseConnection(UUID.randomUUID(), Set.of("EVENT"), null);
    // then
    assertThrows(NullPointerException.class, executable);
  }

  private static Instant getExpiresAt() {
    return Instant.now().plusSeconds(60);
  }
}
