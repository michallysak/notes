package pl.michallysak.notes.application.quarkus.note.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import pl.michallysak.notes.application.quarkus.common.JsonWebTokenProvider;
import pl.michallysak.notes.application.quarkus.common.dto.ErrorResponse;
import pl.michallysak.notes.application.quarkus.note.dto.KeyResponse;
import pl.michallysak.notes.note.domain.event.DomainEvent;
import pl.michallysak.notes.note.domain.event.DomainEventPublisher;
import pl.michallysak.notes.note.domain.event.NotePublicShareRemovedEvent;
import pl.michallysak.notes.note.domain.event.NotePublicShareUpsertedEvent;
import pl.michallysak.notes.note.domain.event.NoteUpdatedEvent;
import pl.michallysak.notes.note.model.NotePublicShare;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.user.service.CurrentUserProvider;

@ApplicationScoped
@RequiredArgsConstructor
public class SseDomainEventPublisher implements DomainEventPublisher {

  public static final String NOTE_PUBLIC_SHARE_UPSERTED_EVENT = "NOTE_PUBLIC_SHARE_UPSERTED_EVENT";
  public static final String NOTE_PUBLIC_SHARE_REMOVED_EVENT = "NOTE_PUBLIC_SHARE_REMOVED_EVENT";
  public static final String NOTE_UPDATED_EVENT = "NOTE_UPDATED_EVENT";
  public static final Set<String> PUBLIC_SHARE_EVENTS =
      Set.of(NOTE_PUBLIC_SHARE_UPSERTED_EVENT, NOTE_PUBLIC_SHARE_REMOVED_EVENT, NOTE_UPDATED_EVENT);

  private static final Duration publicSseTtl = Duration.parse("PT15M");
  private final ObjectMapper objectMapper;
  private final Logger logger;
  private final CurrentUserProvider currentUserProvider;
  private final JsonWebTokenProvider jsonWebTokenProvider;

  private final Map<String, SseConnection> keys = new ConcurrentHashMap<>();

  private volatile Sse sse;

  public KeyResponse createStreamKey(Set<String> requestedEventNames) {
    Instant expiresAt =
        jsonWebTokenProvider
            .getExpired()
            .orElseThrow(
                () -> new IllegalStateException("Failed to determine token expiration time"));

    if (requestedEventNames == null || requestedEventNames.isEmpty()) {
      ErrorResponse errorResponse = new ErrorResponse("At least one event name must be specified");
      Response response =
          Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
      throw new WebApplicationException(response);
    }

    UUID userId = currentUserProvider.getCurrentUserId();
    SseConnection sseConnection = new SseConnection(userId, requestedEventNames, expiresAt);

    String streamKey = sseConnection.getStreamKey();
    keys.put(streamKey, sseConnection);
    logger.info("Created stream key for user %s expiresAt %s".formatted(userId, expiresAt));

    return new KeyResponse(streamKey, expiresAt);
  }

  public KeyResponse createPublicStreamKey(UUID publicShareId) {
    Objects.requireNonNull(publicShareId, "publicShareId cannot be null");
    Instant expiresAt = Instant.now().plus(publicSseTtl);

    PublicNoteSseConnection sseConnection =
        new PublicNoteSseConnection(publicShareId, PUBLIC_SHARE_EVENTS, expiresAt);

    String streamKey = sseConnection.getStreamKey();
    keys.put(streamKey, sseConnection);
    logger.info(
        "Created public stream key for publicShareId %s expiresAt %s"
            .formatted(publicShareId, expiresAt));

    return new KeyResponse(streamKey, expiresAt);
  }

  public void stream(SseEventSink sink, Sse sse, String streamKey) {
    Objects.requireNonNull(sink, "sink cannot be null");
    Objects.requireNonNull(streamKey, "streamKey cannot be null");
    this.sse = Objects.requireNonNull(sse, "sse cannot be null");

    SseConnection connection = bindSinkToConnection(streamKey, sink);

    logger.info("SSE connected: %s with key %s".formatted(connection.getUserId(), streamKey));
  }

  private SseConnection bindSinkToConnection(String streamKey, SseEventSink sink) {
    SseConnection connection = keys.get(streamKey);
    if (connection == null) {
      logger.info("No stream key found %s".formatted(streamKey));
      try {
        sink.close();
      } catch (Exception e) {
        logger.error("Failed to close sink for unknown key %s".formatted(streamKey), e);
      }
      ErrorResponse errorResponse = new ErrorResponse("No stream key found");
      Response response = Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
      throw new WebApplicationException(response);
    }

    if (connection.isExpired()) {
      logger.info("Stream key expired %s".formatted(streamKey));
      keys.remove(streamKey);
      try {
        sink.close();
      } catch (Exception e) {
        logger.error("Failed to close sink for expired key %s".formatted(streamKey), e);
      }
      ErrorResponse errorResponse = new ErrorResponse("No stream expired");
      Response response =
          Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
      throw new WebApplicationException(response);
    }

    connection.setSink(sink);
    return connection;
  }

  @Override
  public void publish(List<DomainEvent<?>> events) {
    if (events == null || events.isEmpty()) {
      logger.info("No events to publish");
      return;
    }
    if (keys.isEmpty()) {
      logger.info("No domain events received");
      return;
    }

    for (DomainEvent<?> event : events) {
      OutboundSseEvent sseEvent;
      try {
        sseEvent = buildEvent(event);
      } catch (Exception e) {
        logger.error("Failed to serialize event payload", e);
        continue;
      }

      Set<UUID> recipients = event.getRecipients();
      if (recipients == null || recipients.isEmpty()) {
        logger.info("No recipient found %s".formatted(event.getRecipients()));
      } else {
        List<SseConnection> connections =
            keys.values().stream()
                .filter(c -> !(c instanceof PublicNoteSseConnection))
                .filter(c -> recipients.contains(c.getUserId()))
                .filter(c -> c.containsEvent(sseEvent.getName()))
                .toList();

        if (connections.isEmpty()) {
          logger.info("No connections found");
        } else {
          send(connections, sseEvent);
        }
      }

      publishToPublicConnections(event, sseEvent);
    }
  }

  private void publishToPublicConnections(DomainEvent<?> event, OutboundSseEvent sseEvent) {
    Optional<UUID> publicShareId = publicShareIdOf(event);
    if (publicShareId.isEmpty()) {
      return;
    }

    List<PublicNoteSseConnection> publicConnections =
        keys.values().stream()
            .filter(PublicNoteSseConnection.class::isInstance)
            .map(PublicNoteSseConnection.class::cast)
            .filter(c -> c.isViewingPublicShare(publicShareId.get()))
            .filter(c -> c.containsEvent(sseEvent.getName()))
            .toList();

    if (publicConnections.isEmpty()) {
      return;
    }

    OutboundSseEvent publicEvent;
    try {
      publicEvent = event.getPayload() instanceof NoteValue ? buildPublicEvent(event) : sseEvent;
    } catch (Exception e) {
      logger.error("Failed to serialize public event payload", e);
      return;
    }

    send(publicConnections, publicEvent);

    if (event instanceof NotePublicShareRemovedEvent) {
      closePublicConnections(publicConnections);
    }
  }

  private Optional<UUID> publicShareIdOf(DomainEvent<?> event) {
    if (event instanceof NotePublicShareUpsertedEvent || event instanceof NoteUpdatedEvent) {
      NoteValue note = (NoteValue) event.getPayload();
      return note.publicShare().map(NotePublicShare::publicShareId);
    }
    if (event instanceof NotePublicShareRemovedEvent removed) {
      return Optional.ofNullable(removed.getPayload().getPublicShareId());
    }
    return Optional.empty();
  }

  private OutboundSseEvent buildPublicEvent(DomainEvent<?> event) throws JsonProcessingException {
    NoteValue payload = (NoteValue) event.getPayload();
    NoteValue publicView =
        NoteValue.builder()
            .id(payload.id())
            .authorId(payload.authorId())
            .title(payload.title())
            .content(payload.content())
            .created(payload.created())
            .updated(payload.updated())
            .pinned(payload.pinned())
            .style(payload.style())
            .shares(Set.of())
            .publicShare(payload.publicShare())
            .build();

    String jsonPayload = objectMapper.writeValueAsString(publicView);
    return sse.newEventBuilder()
        .name(toUpperSnakeCase(event.getClass().getSimpleName()))
        .id(event.getId().toString())
        .data(String.class, jsonPayload)
        .build();
  }

  private void closePublicConnections(List<PublicNoteSseConnection> connections) {
    for (PublicNoteSseConnection connection : connections) {
      keys.remove(connection.getStreamKey());
      SseEventSink sink = connection.getSink();
      if (sink != null) {
        try {
          sink.close();
        } catch (Exception ignored) {
        }
      }
    }
  }

  private void send(List<? extends SseConnection> connections, OutboundSseEvent sseEvent) {
    for (SseConnection connection : connections) {
      try {
        connection.send(sseEvent);
        logger.debug(
            "Sent event %s to user %s, payload %s"
                .formatted(sseEvent.getName(), connection.getUserId(), sseEvent.getData()));
      } catch (SseConnectionException ex) {
        logger.error(
            "Failed to send event to user %s: %s"
                .formatted(connection.getUserId(), ex.getMessage()),
            ex);
        if (ex.isConnectionTermination()) {
          logger.debug("Removed closed sink for key %s".formatted(ex.getStreamKey()));
          keys.remove(ex.getStreamKey());
        }
      } catch (Exception ex) {
        logger.debug("Failed to send event to user %s".formatted(connection.getUserId()), ex);
      }
    }
  }

  @Scheduled(every = "1m")
  void cleanupExpiredKeys() {
    if (keys.isEmpty()) {
      return;
    }
    int before = keys.size();
    keys.entrySet().removeIf(e -> e.getValue().isExpired());
    int after = keys.size();
    if (before != after) {
      logger.debug("Cleaned up %d expired SSE keys".formatted(before - after));
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
