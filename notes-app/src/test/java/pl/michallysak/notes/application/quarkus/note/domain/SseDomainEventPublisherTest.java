package pl.michallysak.notes.application.quarkus.note.domain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.common.JsonWebTokenProvider;
import pl.michallysak.notes.application.quarkus.note.dto.KeyResponse;
import pl.michallysak.notes.note.domain.event.DomainEvent;
import pl.michallysak.notes.user.service.CurrentUserProvider;

@ExtendWith(MockitoExtension.class)
public class SseDomainEventPublisherTest {
  @Mock ObjectMapper objectMapper;
  @Mock Logger logger;
  @Mock CurrentUserProvider currentUserProvider;
  @Mock JsonWebTokenProvider jsonWebTokenProvider;
  @Mock SseEventSink eventSink;
  @Mock Sse sse;
  @InjectMocks SseDomainEventPublisher publisher;

  UUID userId = UUID.randomUUID();
  private static final Set<String> TEST_EVENTS = Set.of("TEST_DOMAIN_EVENT");

  @Test
  void createStreamKey_shouldThrowIllegalStateException_whenTokenExpirationNotAvailable() {
    // given
    when(jsonWebTokenProvider.getExpired()).thenReturn(Optional.empty());
    // when
    Executable executable = () -> publisher.createStreamKey(TEST_EVENTS);
    // then
    assertThrows(IllegalStateException.class, executable);
  }

  @Test
  void createStreamKey_shouldThrowWebApplicationException_whenEventsNull() {
    // given
    when(jsonWebTokenProvider.getExpired()).thenReturn(Optional.of(Instant.now().plusSeconds(60)));
    // when
    Executable executable = () -> publisher.createStreamKey(null);
    // then
    WebApplicationException ex = assertThrows(WebApplicationException.class, executable);
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void createStreamKey_shouldThrowWebApplicationException_whenEventsEmpty() {
    // given
    when(jsonWebTokenProvider.getExpired()).thenReturn(Optional.of(Instant.now().plusSeconds(60)));
    // when
    Executable executable = () -> publisher.createStreamKey(Set.of());
    // then
    WebApplicationException ex = assertThrows(WebApplicationException.class, executable);
    assertEquals(400, ex.getResponse().getStatus());
  }

  @Test
  void createStreamKey_shouldReturnKeyResponse() {
    // given
    when(jsonWebTokenProvider.getExpired()).thenReturn(Optional.of(Instant.now().plusSeconds(60)));
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    // when
    KeyResponse keyResponse = publisher.createStreamKey(TEST_EVENTS);
    // then
    assertNotNull(keyResponse);
    assertNotNull(keyResponse.getKey());
    assertNotNull(keyResponse.getExpiresAt());
    verify(logger).info(contains("Created stream key"));
  }

  @Test
  void stream_shouldThrowNullPointerException_whenSinkIsNull() {
    // when
    Executable executable = () -> publisher.stream(null, sse, "key");
    // then
    assertThrows(NullPointerException.class, executable);
  }

  @Test
  void stream_shouldThrowNullPointerException_whenSseIsNull() {
    // when
    Executable executable = () -> publisher.stream(eventSink, null, "key");
    // then
    assertThrows(NullPointerException.class, executable);
  }

  @Test
  void stream_shouldThrowNullPointerException_whenKeyIsNull() {
    // when
    Executable executable = () -> publisher.stream(eventSink, sse, null);
    // then
    assertThrows(NullPointerException.class, executable);
  }

  @Test
  void stream_shouldAddSinkAndSetSse() {
    // when
    createKeyAndStream();
    // then
    verify(logger).info(contains("SSE connected"));
  }

  @Test
  void stream_shouldThrow404_whenKeyNotFound() {
    // when
    Executable executable = () -> publisher.stream(eventSink, sse, "unknown-key");
    // then
    WebApplicationException ex = assertThrows(WebApplicationException.class, executable);
    assertEquals(404, ex.getResponse().getStatus());
    verify(logger).info(contains("No stream key found"));
  }

  @Test
  void stream_shouldThrow404AndLogError_whenKeyNotFoundAndCloseThrows() {
    // given
    doThrow(new RuntimeException("close fail")).when(eventSink).close();
    // when
    Executable executable = () -> publisher.stream(eventSink, sse, "unknown-key");
    // then
    WebApplicationException ex = assertThrows(WebApplicationException.class, executable);
    assertEquals(404, ex.getResponse().getStatus());
    verify(logger).info(contains("No stream key found"));
    verify(logger).error(contains("Failed to close sink for unknown key"), any(Throwable.class));
  }

  @Test
  void stream_shouldThrow400_whenKeyExpired() {
    // given
    when(jsonWebTokenProvider.getExpired()).thenReturn(Optional.of(Instant.now().minusSeconds(1)));
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    KeyResponse keyResponse = publisher.createStreamKey(TEST_EVENTS);
    // when
    Executable executable = () -> publisher.stream(eventSink, sse, keyResponse.getKey());
    // then
    WebApplicationException ex = assertThrows(WebApplicationException.class, executable);
    assertEquals(400, ex.getResponse().getStatus());
    verify(logger).info(contains("Stream key expired"));
  }

  @Test
  void stream_shouldThrow400AndLogError_whenKeyExpiredAndCloseThrows() throws Exception {
    // given
    when(jsonWebTokenProvider.getExpired()).thenReturn(Optional.of(Instant.now().minusSeconds(1)));
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    KeyResponse keyResponse = publisher.createStreamKey(TEST_EVENTS);
    doThrow(new RuntimeException("close fail")).when(eventSink).close();
    // when
    Executable executable = () -> publisher.stream(eventSink, sse, keyResponse.getKey());
    // then
    WebApplicationException ex = assertThrows(WebApplicationException.class, executable);
    assertEquals(400, ex.getResponse().getStatus());
    verify(logger).info(contains("Stream key expired"));
    verify(logger).error(contains("Failed to close sink for expired key"), any(Throwable.class));
  }

  @Test
  void publish_shouldLogAndReturn_whenEventsNull() {
    // when
    publisher.publish(null);
    // then
    verify(logger).info(contains("No events to publish"));
  }

  @Test
  void publish_shouldLogAndReturn_whenEventsEmpty() {
    // when
    publisher.publish(List.of());
    // then
    verify(logger).info(contains("No events to publish"));
  }

  @Test
  void publish_shouldLogAndReturn_whenNoKeysRegistered() {
    // given
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", Set.of(userId));
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).info(contains("No domain events received"));
  }

  @Test
  void publish_shouldLogAndSkip_whenRecipientsNull() throws Exception {
    // given
    createKeyAndStream();
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", null);
    setupSseEventMock(false);
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).info(contains("No recipient found"));
    verify(eventSink, never()).send(any());
  }

  @Test
  void publish_shouldLogAndSkip_whenRecipientsEmpty() throws Exception {
    // given
    createKeyAndStream();
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", Set.of());
    setupSseEventMock(false);
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).info(contains("No recipient found"));
    verify(eventSink, never()).send(any());
  }

  @Test
  void publish_shouldSendEventToMatchingRecipient() throws Exception {
    // given
    createKeyAndStream();
    when(eventSink.isClosed()).thenReturn(false);
    when(eventSink.send(any())).thenReturn(mock(CompletionStage.class));
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", Set.of(userId));
    setupSseEventMock();
    // when
    publisher.publish(List.of(event));
    // then
    verify(eventSink).send(any());
  }

  @Test
  void publish_shouldNotSend_whenNoConnectionMatchesRecipient() throws Exception {
    // given
    createKeyAndStream();
    UUID eventId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", Set.of(otherUserId));
    setupSseEventMock(false);
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).info(contains("No connections found"));
    verify(eventSink, never()).send(any());
  }

  @Test
  void publish_shouldNotSend_whenConnectionDoesNotMatchEventName() throws Exception {
    // given
    when(jsonWebTokenProvider.getExpired()).thenReturn(Optional.of(Instant.now().plusSeconds(60)));
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    KeyResponse keyResponse = publisher.createStreamKey(Set.of("OTHER_EVENT"));
    publisher.stream(eventSink, sse, keyResponse.getKey());
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", Set.of(userId));
    setupSseEventMock();
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).info(contains("No connections found"));
    verify(eventSink, never()).send(any());
  }

  @Test
  void publish_shouldRemoveConnection_whenSseConnectionTermination() throws Exception {
    // given
    createKeyAndStream();
    when(eventSink.isClosed()).thenReturn(true);
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", Set.of(userId));
    setupSseEventMock();
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).error(contains("Failed to send event to user"), any(Throwable.class));
    verify(logger).debug(contains("Removed closed sink for key"));
  }

  @Test
  void publish_shouldLogError_whenSseConnectionExceptionWithoutTermination() throws Exception {
    // given
    createKeyAndStream();
    when(eventSink.isClosed()).thenReturn(false);
    doThrow(new RuntimeException("send fail")).when(eventSink).send(any());
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", Set.of(userId));
    setupSseEventMock();
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).error(contains("Failed to send event to user"), any(Throwable.class));
    verify(logger, never()).debug(contains("Removed closed sink"));
  }

  @Test
  void publish_shouldLogDebug_whenUnexpectedExceptionInSend() throws Exception {
    // given
    createKeyAndStream();
    when(eventSink.isClosed()).thenThrow(new RuntimeException("unexpected error"));
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", Set.of(userId));
    setupSseEventMock();
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).debug(contains("Failed to send event to user"), any(Throwable.class));
  }

  @Test
  void publish_shouldLogError_whenSerializationFails() throws Exception {
    // given
    createKeyAndStream();
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", Set.of(userId));
    when(objectMapper.writeValueAsString(any()))
        .thenThrow(new RuntimeException("serialization fail"));
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).error(contains("Failed to serialize event payload"), any(Throwable.class));
  }

  // --- cleanupExpiredKeys tests ---

  @Test
  void cleanupExpiredKeys_shouldDoNothing_whenNoKeys() {
    // when
    publisher.cleanupExpiredKeys();
    // then
    verify(logger, never()).debug(anyString());
  }

  @Test
  void cleanupExpiredKeys_shouldRemoveExpiredKeys() {
    // given
    when(jsonWebTokenProvider.getExpired()).thenReturn(Optional.of(Instant.now().minusSeconds(1)));
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    publisher.createStreamKey(TEST_EVENTS);
    // when
    publisher.cleanupExpiredKeys();
    // then
    verify(logger).debug(contains("Cleaned up"));
  }

  @Test
  void cleanupExpiredKeys_shouldNotRemoveValidKeys() {
    // given
    when(jsonWebTokenProvider.getExpired()).thenReturn(Optional.of(Instant.now().plusSeconds(60)));
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    publisher.createStreamKey(TEST_EVENTS);
    // when
    publisher.cleanupExpiredKeys();
    // then
    verify(logger, never()).debug(contains("Cleaned up"));
  }

  private void createKeyAndStream() {
    when(jsonWebTokenProvider.getExpired()).thenReturn(Optional.of(Instant.now().plusSeconds(60)));
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    KeyResponse keyResponse = publisher.createStreamKey(TEST_EVENTS);
    publisher.stream(eventSink, sse, keyResponse.getKey());
  }

  private void setupSseEventMock() throws Exception {
    setupSseEventMock(true);
  }

  private void setupSseEventMock(boolean stubEventName) throws Exception {
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    OutboundSseEvent.Builder builder = mock(OutboundSseEvent.Builder.class, RETURNS_SELF);
    OutboundSseEvent sseEvent = mock(OutboundSseEvent.class);
    if (stubEventName) {
      when(sseEvent.getName()).thenReturn("TEST_DOMAIN_EVENT");
    }
    when(builder.build()).thenReturn(sseEvent);
    when(sse.newEventBuilder()).thenReturn(builder);
  }
}
