package pl.michallysak.notes.application.quarkus.note.resource;

import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import pl.michallysak.notes.application.quarkus.note.controller.NoteController;
import pl.michallysak.notes.application.quarkus.note.domain.SseDomainEventPublisher;
import pl.michallysak.notes.application.quarkus.note.dto.KeyResponse;

@Path("/public/notes")
@ApplicationScoped
@RequiredArgsConstructor
public class PublicNoteSseResource {
  private final SseDomainEventPublisher sseDomainEventPublisher;
  private final NoteController noteController;

  @POST
  @Path("/{publicShareId}/events/keys")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Create public stream key for public note events",
      operationId = "createPublicNoteStreamKey",
      description =
          "Creates a short-lived stream key bound to a public share id for subscribing to public note events (note updates, permission changes, and public-share removal) via Server-Sent Events. The public note must exist and be publicly shared, otherwise a 404 is returned. The response includes the generated stream key and its expiration time.")
  @PermitAll
  public KeyResponse createPublicNoteStreamKey(@PathParam("publicShareId") UUID publicShareId) {
    noteController.getPublicNote(publicShareId);
    return sseDomainEventPublisher.createPublicStreamKey(publicShareId);
  }
}
