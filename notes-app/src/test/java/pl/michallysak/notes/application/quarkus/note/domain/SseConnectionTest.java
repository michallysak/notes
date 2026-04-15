package pl.michallysak.notes.application.quarkus.note.domain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.SseEventSink;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class SseConnectionTest {

  @Test
  void constructor_shouldThrowNullPointerException_whenUserIdIsNull() {
    // when
    Executable event = () -> new SseConnection(null, Set.of("EVENT"), getExpiresAt());
    // then
    assertThrows(NullPointerException.class, event);
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenEventsIsNull() {
    // given
    UUID userId = UUID.randomUUID();
    // when
    Executable executable = () -> new SseConnection(userId, null, getExpiresAt());
    // then
    assertThrows(NullPointerException.class, executable);
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenExpiresAtIsNull() {
    // given
    UUID userId = UUID.randomUUID();
    // when
    Executable event = () -> new SseConnection(userId, Set.of("A"), null);
    // then
    assertThrows(NullPointerException.class, event);
  }

  @Test
  void constructor_shouldCreateSseConnection() {
    // given
    UUID userId = UUID.randomUUID();
    // when
    SseConnection conn = new SseConnection(userId, Set.of("A"), getExpiresAt());
    // then
    assertEquals(userId, conn.getUserId());
    assertNotNull(conn.getStreamKey());
    assertNull(conn.getSink());
  }

  @Test
  void isExpired_shouldReturnFalse_whenNotExpired() {
    // given
    UUID userId = UUID.randomUUID();
    // when
    SseConnection conn = new SseConnection(userId, Set.of("A"), getExpiresAt());
    // then
    assertFalse(conn.isExpired());
  }

  @Test
  void isExpired_shouldReturnTrue_whenExpired() {
    // given
    UUID userId = UUID.randomUUID();
    // when
    SseConnection conn = new SseConnection(userId, Set.of("A"), Instant.now().minusSeconds(1));
    // then
    assertTrue(conn.isExpired());
  }

  @Test
  void containsEvent_shouldReturnTrue_whenEventExists() {
    // given
    UUID userId = UUID.randomUUID();
    // when
    SseConnection conn = new SseConnection(userId, Set.of("A", "B"), getExpiresAt());
    // then
    assertTrue(conn.containsEvent("A"));
    assertTrue(conn.containsEvent("B"));
  }

  @Test
  void containsEvent_shouldReturnFalse_whenEventDoesNotExist() {
    // given
    UUID userId = UUID.randomUUID();
    // when
    SseConnection conn = new SseConnection(userId, Set.of("A"), getExpiresAt());
    // then
    assertFalse(conn.containsEvent("C"));
  }

  @Test
  void send_shouldThrowSseConnectionException_whenSinkIsNull() {
    // given
    UUID userId = UUID.randomUUID();
    SseConnection conn = new SseConnection(userId, Set.of("A"), getExpiresAt());
    OutboundSseEvent event = mock(OutboundSseEvent.class);
    // when
    Executable executable = () -> conn.send(event);
    // then
    SseConnectionException ex = assertThrows(SseConnectionException.class, executable);
    assertTrue(ex.isConnectionTermination());
    assertEquals(conn.getStreamKey(), ex.getStreamKey());
    assertTrue(ex.getMessage().contains("No active SSE connection"));
  }

  @Test
  void send_shouldThrowSseConnectionException_whenExpired() {
    // given
    UUID userId = UUID.randomUUID();
    SseConnection conn = new SseConnection(userId, Set.of("A"), Instant.now().minusSeconds(1));
    SseEventSink sink = mock(SseEventSink.class);
    conn.setSink(sink);
    OutboundSseEvent event = mock(OutboundSseEvent.class);
    // when
    Executable executable = () -> conn.send(event);
    // then
    SseConnectionException ex = assertThrows(SseConnectionException.class, executable);
    assertTrue(ex.isConnectionTermination());
    assertTrue(ex.getMessage().contains("SSE connection has expired"));
  }

  @Test
  void send_shouldThrowSseConnectionException_whenSinkIsClosed() {
    // given
    UUID userId = UUID.randomUUID();
    SseConnection conn = new SseConnection(userId, Set.of("A"), getExpiresAt());
    SseEventSink sink = mock(SseEventSink.class);
    when(sink.isClosed()).thenReturn(true);
    conn.setSink(sink);
    OutboundSseEvent event = mock(OutboundSseEvent.class);
    // when
    Executable executable = () -> conn.send(event);
    // then
    SseConnectionException ex = assertThrows(SseConnectionException.class, executable);
    assertTrue(ex.isConnectionTermination());
    assertTrue(ex.getMessage().contains("SSE connection is closed"));
  }

  @Test
  void send_shouldThrowSseConnectionException_whenSinkIsClosedAndCloseThrows() {
    // given
    UUID userId = UUID.randomUUID();
    SseConnection conn = new SseConnection(userId, Set.of("A"), getExpiresAt());
    SseEventSink sink = mock(SseEventSink.class);
    when(sink.isClosed()).thenReturn(true);
    doThrow(new RuntimeException("close fail")).when(sink).close();
    conn.setSink(sink);
    OutboundSseEvent event = mock(OutboundSseEvent.class);
    Executable executable = () -> conn.send(event);
    // when
    // then
    SseConnectionException ex = assertThrows(SseConnectionException.class, executable);
    assertTrue(ex.isConnectionTermination());
    assertTrue(ex.getMessage().contains("SSE connection is closed"));
  }

  @Test
  void send_shouldSendEvent_whenSinkIsOpenAndNotExpired() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    SseConnection conn = new SseConnection(userId, Set.of("A"), getExpiresAt());
    SseEventSink sink = mock(SseEventSink.class);
    when(sink.isClosed()).thenReturn(false);
    when(sink.send(any())).thenReturn(mock(CompletionStage.class));
    conn.setSink(sink);
    OutboundSseEvent event = mock(OutboundSseEvent.class);
    // when
    conn.send(event);
    // then
    verify(sink).send(event);
  }

  @Test
  void send_shouldThrowSseConnectionException_whenSendThrows() {
    // given
    UUID userId = UUID.randomUUID();
    SseConnection conn = new SseConnection(userId, Set.of("A"), getExpiresAt());
    SseEventSink sink = mock(SseEventSink.class);
    when(sink.isClosed()).thenReturn(false);
    doThrow(new RuntimeException("send fail")).when(sink).send(any());
    conn.setSink(sink);
    OutboundSseEvent event = mock(OutboundSseEvent.class);
    // when
    Executable executable = () -> conn.send(event);
    // then
    SseConnectionException ex = assertThrows(SseConnectionException.class, executable);
    assertFalse(ex.isConnectionTermination());
    assertTrue(ex.getMessage().contains("Failed to send SSE event"));
    assertNotNull(ex.getCause());
  }

  @Test
  void setSink_shouldUpdateSink() {
    // given
    UUID userId = UUID.randomUUID();
    SseConnection conn = new SseConnection(userId, Set.of("A"), getExpiresAt());
    assertNull(conn.getSink());
    SseEventSink sink = mock(SseEventSink.class);
    // when
    conn.setSink(sink);
    // then
    assertEquals(sink, conn.getSink());
  }

  @Test
  void getStreamKey_shouldReturnUniqueKeys() {
    // given
    UUID userId = UUID.randomUUID();
    // when
    SseConnection conn1 = new SseConnection(userId, Set.of("A"), getExpiresAt());
    SseConnection conn2 = new SseConnection(userId, Set.of("A"), getExpiresAt());
    // then
    assertNotEquals(conn1.getStreamKey(), conn2.getStreamKey());
  }

  private static Instant getExpiresAt() {
    return Instant.now().plusSeconds(60);
  }
}
