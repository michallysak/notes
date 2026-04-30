package pl.michallysak.notes.application.quarkus.note.dto;

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
@Schema(description = "Shared user access for a note")
public class NoteShareResponse {
  @Schema(description = "Shared user id", examples = "b3b6c8e2-8c2e-4e2a-9b2e-8c2e4e2a9b2e")
  private UUID userId;

  @Schema(description = "Shared user email", examples = "user@example.com")
  private String email;

  @Schema(description = "Granted permissions")
  private Set<NotePermission> permissions;
}
