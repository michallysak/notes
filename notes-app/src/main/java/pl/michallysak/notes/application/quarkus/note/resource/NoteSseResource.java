package pl.michallysak.notes.application.quarkus.note.resource;

import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import pl.michallysak.notes.application.quarkus.note.domain.SseDomainEventPublisher;

@Path("/notes/events")
@ApplicationScoped
@RequiredArgsConstructor
public class NoteSseResource {
  private final SseDomainEventPublisher sseDomainEventPublisher;

  @Blocking
  @GET
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @Operation(
      summary = "Register for note events",
      operationId = "registerForNoteEvents",
      description =
          "Registers the client to receive real-time updates about note events via Server-Sent Events (SSE).")
  @PermitAll
  public void stream(
      @Context SseEventSink eventSink, @Context Sse sse, @QueryParam("key") String key) {
    sseDomainEventPublisher.stream(eventSink, sse, key);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Authenticated
  @Operation(
      summary = "Create stream key for note events",
      operationId = "createStreamKey",
      description =
          "Creates a stream key for subscribing to note events. The client must provide a set of event names they wish to subscribe to. The response includes the generated stream key and its expiration time.")
  public KeyResponse createStreamKey(Set<String> requestedEvents) {
    return sseDomainEventPublisher.createStreamKey(requestedEvents);
  }
}
