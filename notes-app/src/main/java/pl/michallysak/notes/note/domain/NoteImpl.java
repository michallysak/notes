package pl.michallysak.notes.note.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.validator.NoteValidator;
import pl.michallysak.notes.note.validator.NoteValidatorImpl;

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

    private static final NoteValidator NOTE_VALIDATOR = new NoteValidatorImpl();

    public static Note create(CreateNote createNote) {
        NOTE_VALIDATOR.validateCreateNote(createNote);
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
        NOTE_VALIDATOR.validateNoteUpdate(id, noteUpdate, this);
        this.title = noteUpdate.title();
        this.content = noteUpdate.content();
        if (noteUpdate.pinned() != null) {
            this.isPinned = noteUpdate.pinned();
        }
        this.updated = OffsetDateTime.now();
    }
}
