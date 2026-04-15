package pl.michallysak.notes.application.quarkus.note.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SseConnectionExceptionTest {

  @Test
  void constructor_shouldSetFieldsWithoutCause() {
    // when
    SseConnectionException ex = new SseConnectionException("key-1", "test message", true);
    // then
    assertEquals("key-1", ex.getStreamKey());
    assertTrue(ex.isConnectionTermination());
    assertTrue(ex.getMessage().contains("test message"));
    assertTrue(ex.getMessage().contains("key-1"));
    assertNull(ex.getCause());
  }

  @Test
  void constructor_shouldSetFieldsWithCause() {
    // given
    RuntimeException cause = new RuntimeException("root cause");
    // when
    SseConnectionException ex = new SseConnectionException("key-2", "test message", cause, false);
    // then
    assertEquals("key-2", ex.getStreamKey());
    assertFalse(ex.isConnectionTermination());
    assertTrue(ex.getMessage().contains("test message"));
    assertTrue(ex.getMessage().contains("key-2"));
    assertEquals(cause, ex.getCause());
  }

  @Test
  void constructor_shouldSetFieldsWithNullCause() {
    // when
    SseConnectionException ex = new SseConnectionException("key-3", "msg", null, true);
    // then
    assertEquals("key-3", ex.getStreamKey());
    assertTrue(ex.isConnectionTermination());
    assertTrue(ex.getMessage().contains("msg"));
    assertNull(ex.getCause());
  }
}
