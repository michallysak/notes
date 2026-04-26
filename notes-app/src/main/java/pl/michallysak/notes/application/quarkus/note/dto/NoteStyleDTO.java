package pl.michallysak.notes.application.quarkus.note.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Schema(description = "Style of the note")
public class NoteStyleDTO {
  @Schema(description = "Color of the note in hex format", examples = "#FF5733")
  private String color;
}
