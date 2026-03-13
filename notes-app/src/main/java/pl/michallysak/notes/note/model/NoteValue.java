package pl.michallysak.notes.note.model;


import lombok.Builder;
import pl.michallysak.notes.note.domain.Note;

import java.time.OffsetDateTime;
import java.util.Optional;

@Builder
public record NoteValue(
        String title,
        String content,
        OffsetDateTime created,
        Optional<OffsetDateTime> updated,
        boolean pinned
) {
    public static NoteValue from(Note note) {
        return NoteValue.builder()
                .title(note.getTitle())
                .content(note.getContent())
                .created(note.getCreated())
                .pinned(note.isPinned())
                .updated(note.getUpdated())
                .build();
    }
}

