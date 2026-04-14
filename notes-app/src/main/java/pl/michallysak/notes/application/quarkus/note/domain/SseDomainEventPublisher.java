package pl.michallysak.notes.application.quarkus.note.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import pl.michallysak.notes.note.domain.event.DomainEvent;
import pl.michallysak.notes.note.domain.event.DomainEventPublisher;
import pl.michallysak.notes.user.service.CurrentUserProvider;

@ApplicationScoped
@RequiredArgsConstructor
public class SseDomainEventPublisher implements DomainEventPublisher {

  private final ObjectMapper objectMapper;
  private final Logger logger;
  private final CurrentUserProvider currentUserProvider;

  private final Map<UUID, SseEventSink> sinks = new ConcurrentHashMap<>();

  private volatile Sse sse;

  public void register(SseEventSink sink, Sse sse) {
    this.sse = sse;

    Objects.requireNonNull(sink, "sink cannot be null");
    Objects.requireNonNull(sse, "sse cannot be null");

    UUID userId = currentUserProvider.getCurrentUserId();
    sinks.put(userId, sink);

    logger.info("SSE connected: " + userId);
  }

  @Override
  public void publish(List<DomainEvent<?>> events) {
    if (events == null || events.isEmpty() ) {
      logger.info("No domain events received");
      return;
    }
    if (sinks.isEmpty()) {
      logger.info("No connected clients to receive events");
      return;
    }

    for (var event : events) {
      OutboundSseEvent sseEvent;
      try {
        sseEvent = buildEvent(event);
      } catch (Exception e) {
        logger.error("Failed to serialize event payload", e);
        continue;
      }

      Set<UUID> uuids = Optional.ofNullable(event.getRecipients()).orElse(sinks.keySet());
      uuids.forEach(recipient -> send(recipient, sseEvent));
    }
  }

  private void send(UUID userId, OutboundSseEvent event) {
    var sink = sinks.get(userId);
    if (sink == null) {
      logger.debug("No sink found for user %s".formatted(userId));
      return;
    }

    if (sink.isClosed()) {
      removeSink(userId, sink, "Removed closed sink for user %s".formatted(userId));
      return;
    }

    try {
      sink.send(event);
      logger.debug(
          "Sent event %s to user %s, payload %s"
              .formatted(event.getName(), userId, event.getData()));
    } catch (Exception e) {
      logger.debug("Failed to send event to user %s".formatted(userId), e);
    }
  }

  private void removeSink(UUID userId, SseEventSink sink, String reason) {
    try {
      sink.close();
    } catch (Exception e) {
      logger.error(reason, e);
    } finally {
      //noinspection resource
      sinks.remove(userId);
      logger.debug(reason);
    }
  }

  private OutboundSseEvent buildEvent(DomainEvent<?> event) throws JsonProcessingException {
    String eventName = toUpperSnakeCase(event.getClass().getSimpleName());
    String jsonPayload = objectMapper.writeValueAsString(event.getPayload());

    return sse.newEventBuilder()
        .name(eventName)
        .id(event.getId().toString())
        .data(String.class, jsonPayload)
        .build();
  }

  private String toUpperSnakeCase(String input) {
    return input.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase();
  }
}
