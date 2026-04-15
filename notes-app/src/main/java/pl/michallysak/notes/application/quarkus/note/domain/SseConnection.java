package pl.michallysak.notes.application.quarkus.note.domain;

import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.SseEventSink;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public class SseConnection {
  @Getter private final UUID userId;
  @Getter private final String streamKey;
  private final Set<String> events;
  private final Instant expiresAt;
  @Getter @Setter private volatile SseEventSink sink;

  SseConnection(UUID userId, Set<String> events, Instant expiresAt) {
    this.streamKey = UUID.randomUUID().toString();
    this.userId = Objects.requireNonNull(userId);
    this.events = Objects.requireNonNull(events);
    this.expiresAt = Objects.requireNonNull(expiresAt);
  }

  boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public boolean containsEvent(String eventName) {
    return events.contains(eventName);
  }

  public void send(OutboundSseEvent sseEvent) throws SseConnectionException {
    SseEventSink sink = this.sink;
    if (sink == null) {
      throw new SseConnectionException(streamKey, "No active SSE connection", true);
    }

    if (isExpired()) {
      throw new SseConnectionException(streamKey, "SSE connection has expired", true);
    }

    if (sink.isClosed()) {
      try {
        sink.close();
      } catch (Exception ignored) {
      }
      throw new SseConnectionException(streamKey, "SSE connection is closed", true);
    }

    try {
      sink.send(sseEvent);
    } catch (Exception e) {
      throw new SseConnectionException(streamKey, "Failed to send SSE event: ", e, false);
    }
  }
}
