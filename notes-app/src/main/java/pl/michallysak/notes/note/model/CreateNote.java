package pl.michallysak.notes.note.model;


import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateNote (
        String title,
        String content,
        UUID authorId
) {

}


