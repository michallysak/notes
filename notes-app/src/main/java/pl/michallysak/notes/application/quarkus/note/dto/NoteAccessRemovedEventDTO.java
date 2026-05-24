package pl.michallysak.notes.application.quarkus.note.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Note access removed domain event")
public interface NoteAccessRemovedEventDTO
    extends DomainEventDTO<AccessRemovedEventPayloadResponse> {
  String TYPE = "NOTE_ACCESS_REMOVED_EVENT";

  @Override
  @Schema(
      required = true,
      enumeration = {TYPE})
  default String getType() {
    return TYPE;
  }
}
