package pl.michallysak.notes.application.quarkus.note.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Note updated domain event")
public interface NoteUpdatedEventDTO extends DomainEventDTO<NoteResponse> {
  String TYPE = "NOTE_UPDATED_EVENT";

  @Override
  @Schema(
      required = true,
      enumeration = {TYPE})
  default String getType() {
    return TYPE;
  }
}
