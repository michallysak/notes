package pl.michallysak.notes.note.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Getter
@ToString
@Builder(access = AccessLevel.PRIVATE)
public class NoteImpl implements Note {
    private final UUID id;
    private final OffsetDateTime created;
    private String title;
    private String content;
    private OffsetDateTime updated;
    private boolean isPinned;

    public static Note create(CreateNote createNote) {
        return NoteImpl.builder()
                .id(UUID.randomUUID())
                .title(createNote.title())
                .content(createNote.content())
                .created(OffsetDateTime.now())
                .updated(null)
                .isPinned(false)
                .build();
    }

    @Override
    public Optional<OffsetDateTime> getUpdated() {
        return Optional.ofNullable(updated);
    }

    @Override
    public void update(NoteUpdate noteUpdate) {
        this.title = noteUpdate.title();
        this.content = noteUpdate.content();
        this.isPinned = noteUpdate.pinned();
        this.updated = OffsetDateTime.now();
    }
}
