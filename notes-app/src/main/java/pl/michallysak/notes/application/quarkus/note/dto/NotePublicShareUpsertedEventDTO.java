package pl.michallysak.notes.application.quarkus.note.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Note public share upserted domain event")
public interface NotePublicShareUpsertedEventDTO extends DomainEventDTO<NoteResponse> {
  String TYPE = "NOTE_PUBLIC_SHARE_UPSERTED_EVENT";

  @Override
  @Schema(
      required = true,
      enumeration = {TYPE})
  default String getType() {
    return TYPE;
  }
}
