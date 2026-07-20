package pl.michallysak.notes.application.quarkus.note.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Note public share removed domain event")
public interface NotePublicShareRemovedEventDTO
    extends DomainEventDTO<PublicShareRemovedEventPayloadResponse> {
  String TYPE = "NOTE_PUBLIC_SHARE_REMOVED_EVENT";

  @Override
  @Schema(
      required = true,
      enumeration = {TYPE})
  default String getType() {
    return TYPE;
  }
}
