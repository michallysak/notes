package pl.michallysak.notes.note.domain;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface Note extends NoteActions {
    UUID getId();

    String getTitle();

    String getContent();

    OffsetDateTime getCreated();

    Optional<OffsetDateTime> getUpdated();

    boolean isPinned();
}
