package pl.michallysak.notes.application.quarkus.note.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Schema(description = "Response with note details")
public class NoteResponse {
  @Schema(description = "Note id", example = "b3b6c8e2-8c2e-4e2a-9b2e-8c2e4e2a9b2e")
  private UUID id;

  @Schema(description = "Title of the note", example = "Shopping List")
  private String title;

  @Schema(description = "Content of the note", example = "Milk, Bread, Eggs")
  private String content;

  @Schema(description = "Creation timestamp of the note", example = "2024-06-01T12:00:00Z")
  private OffsetDateTime created;

  @Schema(description = "Last update timestamp of the note", example = "2024-06-02T15:30:00Z")
  private OffsetDateTime updated;

  @Schema(description = "Indicates if the note is pinned", example = "true")
  private boolean pinned;
}
