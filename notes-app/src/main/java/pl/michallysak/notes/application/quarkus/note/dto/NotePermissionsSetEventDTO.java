package pl.michallysak.notes.application.quarkus.note.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Note permissions set domain event")
public interface NotePermissionsSetEventDTO extends DomainEventDTO<PermissionEventPayloadResponse> {
  String TYPE = "NOTE_PERMISSIONS_SET_EVENT";

  @Override
  @Schema(
      required = true,
      enumeration = {TYPE})
  default String getType() {
    return TYPE;
  }
}
