package pl.michallysak.notes.application.quarkus.note.dto;

import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.DiscriminatorMapping;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
    oneOf = {NoteCreatedEventDTO.class},
    discriminatorProperty = "type",
    discriminatorMapping = {
      @DiscriminatorMapping(value = NoteCreatedEventDTO.TYPE, schema = NoteCreatedEventDTO.class)
    })
public interface DomainEventDTO<T> {

  @Schema(required = true)
  UUID getId();

  @Schema(required = true)
  String getType();

  @Schema(required = true)
  T getPayload();
}
