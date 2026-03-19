package pl.michallysak.notes.note.model;


import lombok.Builder;

import java.util.UUID;

@Builder
public record NoteUpdate(
        String title,
        String content,
        Boolean pinned,
        UUID actingUserId
) {

}


