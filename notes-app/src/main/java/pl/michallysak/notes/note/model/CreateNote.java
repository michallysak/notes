package pl.michallysak.notes.note.model;


import lombok.Builder;

@Builder
public record CreateNote(
        String title,
        String content
) {

}


