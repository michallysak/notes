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
@Schema(description = "Request to create a note")
public class CreateNoteRequest {
    @Schema(description = "Title of the note", example = "Shopping List", required = true)
    private String title;

    @Schema(description = "Content of the note", example = "Milk, Bread, Eggs", required = true)
    private String content;
}
