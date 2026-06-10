package pl.michallysak.notes.application.quarkus.note.attachment.resource;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import pl.michallysak.notes.application.quarkus.common.dto.ErrorResponse;
import pl.michallysak.notes.application.quarkus.common.openapi.OpenApiConfig;
import pl.michallysak.notes.application.quarkus.note.attachment.controller.AttachmentContent;
import pl.michallysak.notes.application.quarkus.note.attachment.controller.NoteAttachmentController;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.AttachmentResponse;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.CreateAttachmentMultipartForm;
import pl.michallysak.notes.common.exception.ValidationException;

@Tag(name = "Attachments API", description = "Operations on note attachments")
@Path("/attachments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RequiredArgsConstructor
@Authenticated
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class NoteAttachmentResource {
  private final NoteAttachmentController noteAttachmentController;

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Operation(summary = "Create attachment", operationId = "createAttachment")
  @APIResponse(
      responseCode = "201",
      description = "Attachment created",
      content = @Content(schema = @Schema(implementation = AttachmentResponse.class)))
  @APIResponse(
      responseCode = "400",
      description = "Invalid request",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public AttachmentResponse createAttachment(@MultipartForm CreateAttachmentMultipartForm form) {
    return noteAttachmentController.createAttachment(form);
  }

  @GET
  @Path("/{id}")
  @Operation(
      summary = "Get item metadata or binary content",
      operationId = "getAttachmentOrContent")
  @APIResponse(
      responseCode = "200",
      description = "Attachment response",
      content = {
        @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = AttachmentResponse.class)),
        @Content(
            mediaType = MediaType.APPLICATION_OCTET_STREAM,
            schema = @Schema(type = SchemaType.STRING, format = "binary"))
      })
  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
  public Response getAttachmentOrContent(
      @PathParam("id") UUID id, @HeaderParam("Accept") String accept) {
    return routeAttachmentOrContent(id, accept);
  }

  @GET
  @Operation(summary = "List attachments for note", operationId = "getAttachmentsForNote")
  @APIResponse(
      responseCode = "200",
      description = "List of attachment metas",
      content =
          @Content(
              schema = @Schema(implementation = AttachmentResponse.class, type = SchemaType.ARRAY)))
  public List<AttachmentResponse> getAttachmentsForNote(@QueryParam("noteId") UUID noteId) {
    return noteAttachmentController.getAttachmentsForNote(noteId);
  }

  @DELETE
  @Path("/{id}")
  @Operation(summary = "Delete attachment", operationId = "deleteAttachment")
  @APIResponse(responseCode = "204", description = "Attachment deleted")
  public void deleteAttachment(@PathParam("id") UUID id) {
    noteAttachmentController.deleteAttachment(id);
  }

  Response routeAttachmentOrContent(UUID id, String accept) {
    if (accept == null) {
      throw new ValidationException("Accept header is required");
    }

    if (accept.contains(MediaType.APPLICATION_OCTET_STREAM)) {
      AttachmentContent content = noteAttachmentController.downloadAttachmentContent(id);
      String type =
          Optional.ofNullable(content.contentType()).orElse(MediaType.APPLICATION_OCTET_STREAM);
      return Response.ok(content.value())
          .type(type)
          .header("Content-Disposition", "attachment; filename=\"" + content.fileName() + "\"")
          .build();
    }

    if (accept.contains(MediaType.APPLICATION_JSON)) {
      AttachmentResponse meta = noteAttachmentController.getAttachment(id);
      return Response.ok(meta).type(MediaType.APPLICATION_JSON).build();
    }

    throw new ValidationException("Unsupported Accept header value: " + accept);
  }
}
