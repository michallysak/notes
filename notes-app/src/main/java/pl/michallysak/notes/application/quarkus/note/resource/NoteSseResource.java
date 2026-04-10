package pl.michallysak.notes.application.quarkus.note.resource;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import pl.michallysak.notes.application.quarkus.note.domain.SseDomainEventPublisher;

@Path("/notes/events")
@ApplicationScoped
@Authenticated
@RequiredArgsConstructor
public class NoteSseResource {
  private final SseDomainEventPublisher sseDomainEventPublisher;

  @GET
  @Produces(MediaType.SERVER_SENT_EVENTS)
  @Operation(
      summary = "Register for note events",
      operationId = "registerForNoteEvents",
      description =
          "Registers the client to receive real-time updates about note events via Server-Sent Events (SSE).")
  public void stream(@Context SseEventSink eventSink, @Context Sse sse) {
    sseDomainEventPublisher.register(eventSink, sse);
  }
}
