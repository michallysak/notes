package pl.michallysak.notes.application.quarkus.note.dto;

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
@Schema(description = "Request to set note public permissions")
public class SetNotePublicRequest {

  @NotEmpty
  @Schema(description = "Permissions to set for the public note", required = true)
  private Set<NotePermission> permissions;
}
