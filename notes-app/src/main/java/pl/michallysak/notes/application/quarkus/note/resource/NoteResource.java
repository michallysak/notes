package pl.michallysak.notes.application.quarkus.note.resource;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import pl.michallysak.notes.application.quarkus.common.dto.ErrorResponse;
import pl.michallysak.notes.application.quarkus.common.openapi.OpenApiConfig;
import pl.michallysak.notes.application.quarkus.note.controller.NoteController;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;

@Tag(name = "Notes API", description = "Operations on notes")
@Path("/notes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RequiredArgsConstructor
@Authenticated
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class NoteResource {
  private final NoteController noteController;

  @POST
  @Operation(
      summary = "Create a new note",
      operationId = "createNote",
      description = "Creates a new note with the provided title and content")
  @APIResponse(
      responseCode = "201",
      description = "Note created",
      content = @Content(schema = @Schema(implementation = NoteResponse.class)))
  @APIResponse(
      responseCode = "400",
      description = "Invalid request",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public NoteResponse createNote(CreateNoteRequest request) {
    return noteController.createNote(request);
  }

  @GET
  @Operation(
      summary = "Get all notes",
      operationId = "getNotes",
      description = "Retrieves a list of all notes")
  @APIResponse(
      responseCode = "200",
      description = "List of notes",
      content = @Content(schema = @Schema(implementation = NoteResponse.class)))
  public List<NoteResponse> getNotes() {
    return noteController.getNotes();
  }

  @GET
  @Path("/{id}")
  @Operation(
      summary = "Get note by id",
      operationId = "getNote",
      description = "Retrieves a note by its id")
  @APIResponse(
      responseCode = "200",
      description = "Note found",
      content = @Content(schema = @Schema(implementation = NoteResponse.class)))
  @APIResponse(
      responseCode = "404",
      description = "Note not found",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public NoteResponse getNote(@PathParam("id") UUID id) {
    return noteController.getNote(id);
  }

  @PUT
  @Path("/{id}")
  @Operation(
      summary = "Update note by id",
      operationId = "updateNote",
      description = "Updates a note's title and content by its id")
  @APIResponse(
      responseCode = "200",
      description = "Note updated",
      content = @Content(schema = @Schema(implementation = NoteResponse.class)))
  @APIResponse(
      responseCode = "404",
      description = "Note not found",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public NoteResponse updateNote(@PathParam("id") UUID id, NoteUpdateRequest request) {
    return noteController.updateNote(id, request);
  }

  @DELETE
  @Path("/{id}")
  @Operation(
      summary = "Delete note by id",
      operationId = "deleteNote",
      description = "Deletes a note by its id")
  @APIResponse(
      responseCode = "204",
      description = "Note deleted",
      content = @Content(schema = @Schema(implementation = NoteResponse.class)))
  @APIResponse(
      responseCode = "404",
      description = "Note not found",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public void deleteNote(@PathParam("id") UUID id) {
    noteController.deleteNote(id);
  }
}
