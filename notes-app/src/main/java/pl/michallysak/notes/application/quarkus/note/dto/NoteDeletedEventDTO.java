package pl.michallysak.notes.application.quarkus.note.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Note deleted domain event")
public interface NoteDeletedEventDTO extends DomainEventDTO<NoteResponse> {
  String TYPE = "NOTE_DELETED_EVENT";

  @Override
  @Schema(
      required = true,
      enumeration = {TYPE})
  default String getType() {
    return TYPE;
  }
}
