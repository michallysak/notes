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
@Schema(description = "Request to update a note")
public class NoteUpdateRequest {
    @Schema(description = "Title of the note", example = "Updated Title")
    private String title;

    @Schema(description = "Content of the note", example = "Updated Content")
    private String content;

    @Schema(description = "Indicates if the note is pinned", example = "true")
    private Boolean pinned;
}
