package pl.michallysak.notes.application.quarkus.note.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Access removed event payload")
public class AccessRemovedEventPayloadResponse {
  @Schema(description = "Note ID", examples = "550e8400-e29b-41d4-a716-446655440000")
  private UUID noteId;

  @Schema(description = "Target user ID", examples = "550e8400-e29b-41d4-a716-446655440001")
  private UUID targetUserId;
}
