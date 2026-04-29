package pl.michallysak.notes.application.quarkus.note.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import pl.michallysak.notes.note.model.NotePermission;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Schema(description = "Request to set note permissions for a target user")
public class SetNotePermissionsRequest {
  @NotNull
  @Schema(
      description = "User id to grant note access to",
      examples = "b3b6c8e2-8c2e-4e2a-9b2e-8c2e4e2a9b2e",
      required = true)
  private UUID targetUserId;

  @NotEmpty
  @Schema(description = "Permissions for the target user", required = true)
  private Set<NotePermission> permissions;
}
