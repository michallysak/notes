package pl.michallysak.notes.application.quarkus.note.domain;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.note.domain.event.DomainEvent;
import pl.michallysak.notes.user.service.CurrentUserProvider;

@ExtendWith(MockitoExtension.class)
public class SseDomainEventPublisherTest {
  @Mock ObjectMapper objectMapper;
  @Mock Logger logger;
  @Mock CurrentUserProvider currentUserProvider;
  @Mock SseEventSink eventSink;
  @Mock Sse sse;
  @InjectMocks SseDomainEventPublisher publisher;

  UUID userId = UUID.randomUUID();

  @Test
  void register_shouldAddSinkAndSetSse() {
    // given
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    // when
    publisher.register(eventSink, sse);
    // then
    verify(logger).info(contains(userId.toString()));
  }

  @Test
  void publish_shouldLogAndReturnIfNoEvents() {
    // given
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    publisher.register(eventSink, sse);
    // when
    publisher.publish(null);
    publisher.publish(List.of());
    // then
    verify(logger, atLeastOnce()).info(contains("No domain events received"));
  }

  @Test
  void publish_shouldSendEventToAllIfRecipientsNull() throws Exception {
    // given
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    publisher.register(eventSink, sse);
    when(eventSink.send(any())).thenReturn(mock(CompletionStage.class));
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", null);
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    OutboundSseEvent.Builder builder = mock(OutboundSseEvent.Builder.class, RETURNS_SELF);
    when(builder.build()).thenReturn(mock(OutboundSseEvent.class));
    when(sse.newEventBuilder()).thenReturn(builder);
    // when
    publisher.publish(List.of(event));
    // then
    verify(eventSink).send(any());
  }

  @Test
  void publish_shouldSendEventToSpecificRecipients() throws Exception {
    // given
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    publisher.register(eventSink, sse);
    when(eventSink.send(any())).thenReturn(mock(CompletionStage.class));
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", List.of(userId));
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    OutboundSseEvent.Builder builder = mock(OutboundSseEvent.Builder.class, RETURNS_SELF);
    when(builder.build()).thenReturn(mock(OutboundSseEvent.class));
    when(sse.newEventBuilder()).thenReturn(builder);
    // when
    publisher.publish(List.of(event));
    // then
    verify(eventSink).send(any());
  }

  @Test
  void publish_shouldRemoveSinkIfClosed() throws Exception {
    // given
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    publisher.register(eventSink, sse);
    when(eventSink.isClosed()).thenReturn(true);
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", List.of(userId));
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    OutboundSseEvent.Builder builder = mock(OutboundSseEvent.Builder.class, RETURNS_SELF);
    when(builder.build()).thenReturn(mock(OutboundSseEvent.class));
    when(sse.newEventBuilder()).thenReturn(builder);
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).debug(contains("Removed closed sink"));
  }

  @Test
  void publish_shouldLogAndReturnIfEventsNull() {
    // when
    publisher.publish(null);
    // then
    verify(logger).info(contains("No domain events received"));
  }

  @Test
  void publish_shouldLogAndReturnIfEventsEmpty() {
    // when
    publisher.publish(List.of());
    // then
    verify(logger).info(contains("No domain events received"));
  }

  @Test
  void publish_shouldLogAndReturnIfSinksEmpty() {
    // when
    // No sink registered
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", null);
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).info(contains("No domain events received"));
  }

  @Test
  void publish_shouldLogAndReturnIfSinkNull() throws Exception {
    // given
    // Register a sink for a different user
    UUID otherUserId = UUID.randomUUID();
    when(currentUserProvider.getCurrentUserId()).thenReturn(otherUserId);
    publisher.register(eventSink, sse);
    // Now publish to a userId that is not present
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", List.of(UUID.randomUUID()));
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    OutboundSseEvent.Builder builder = mock(OutboundSseEvent.Builder.class, RETURNS_SELF);
    when(builder.build()).thenReturn(mock(OutboundSseEvent.class));
    when(sse.newEventBuilder()).thenReturn(builder);
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).debug(contains("No sink found for user"));
  }

  @Test
  void publish_shouldRemoveSinkOnSendExceptionally() throws Exception {
    // given
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    publisher.register(eventSink, sse);
    when(eventSink.isClosed()).thenReturn(false);
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", List.of(userId));
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    OutboundSseEvent.Builder builder = mock(OutboundSseEvent.Builder.class, RETURNS_SELF);
    when(builder.build()).thenReturn(mock(OutboundSseEvent.class));
    when(sse.newEventBuilder()).thenReturn(builder);
    // Simulate synchronous exception
    doThrow(new RuntimeException("fail")).when(eventSink).send(any());
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).debug(contains("Failed to send event to user"), any(Throwable.class));
  }

  @Test
  void publish_shouldLogErrorIfCloseThrows() throws Exception {
    // given
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    publisher.register(eventSink, sse);
    doThrow(new RuntimeException("close fail")).when(eventSink).close();
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", List.of(userId));
    when(objectMapper.writeValueAsString(any())).thenReturn("{}");
    OutboundSseEvent.Builder builder = mock(OutboundSseEvent.Builder.class, RETURNS_SELF);
    when(builder.build()).thenReturn(mock(OutboundSseEvent.class));
    when(sse.newEventBuilder()).thenReturn(builder);
    when(eventSink.isClosed()).thenReturn(true);
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).error(contains("Removed closed sink"), any(Throwable.class));
  }

  @Test
  void publish_shouldLogErrorIfSerializationFails() throws Exception {
    // given
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    publisher.register(eventSink, sse);
    UUID eventId = UUID.randomUUID();
    DomainEvent<?> event = new TestDomainEvent<>(eventId, "payload", List.of(userId));
    when(objectMapper.writeValueAsString(any()))
        .thenThrow(new RuntimeException("serialization fail"));
    // when
    publisher.publish(List.of(event));
    // then
    verify(logger).error(contains("Failed to serialize event payload"), any(Throwable.class));
  }
}
