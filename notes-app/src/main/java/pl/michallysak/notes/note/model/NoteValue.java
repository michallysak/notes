package pl.michallysak.notes.note.model;


import lombok.Builder;
import pl.michallysak.notes.note.domain.Note;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Builder
public record NoteValue(
        UUID id,
        String title,
        String content,
        OffsetDateTime created,
        Optional<OffsetDateTime> updated,
        boolean pinned
) {
    public static NoteValue from(Note note) {
        return NoteValue.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .created(note.getCreated())
                .pinned(note.isPinned())
                .updated(note.getUpdated())
                .build();
    }
}

