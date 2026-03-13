package pl.michallysak.notes.note.model;


import lombok.Builder;

@Builder
public record NoteUpdate(
        String title,
        String content,
        boolean pinned
) {

}


