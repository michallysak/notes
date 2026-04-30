package pl.michallysak.notes.application.quarkus.note.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
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
  @NotBlank
  @Email
  @Schema(
      description = "Email of user to grant note access to",
      examples = "user@example.com",
      required = true)
  private String email;

  @NotEmpty
  @Schema(description = "Permissions for the target user", required = true)
  private Set<NotePermission> permissions;
}
