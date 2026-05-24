package pl.michallysak.notes.application.quarkus.note.dto;

import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import pl.michallysak.notes.note.model.NotePermission;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Permission event payload")
public class PermissionEventPayloadResponse {
  @Schema(description = "Note ID", examples = "550e8400-e29b-41d4-a716-446655440000")
  private UUID noteId;

  @Schema(description = "Target user ID", examples = "550e8400-e29b-41d4-a716-446655440001")
  private UUID targetUserId;

  @Schema(description = "Permissions granted")
  private Set<NotePermission> permissions;
}
