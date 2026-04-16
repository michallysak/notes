package pl.michallysak.notes.application.quarkus.note.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Note created domain event")
public interface NoteCreatedEventDTO extends DomainEventDTO<NoteResponse> {
  String TYPE = "NOTE_CREATED_EVENT";

  @Override
  @Schema(
      required = true,
      enumeration = {TYPE})
  default String getType() {
    return TYPE;
  }
}
