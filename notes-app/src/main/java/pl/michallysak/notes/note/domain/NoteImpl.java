package pl.michallysak.notes.note.domain;

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
public class NoteImpl implements Note {
    private final UUID id;
    private final OffsetDateTime created;
    private String title;
    private String content;
    private OffsetDateTime updated;
    private boolean isPinned;
    private final UUID authorId;

    private static final NoteValidator NOTE_VALIDATOR = new NoteValidatorImpl();

    public NoteImpl(CreateNote createNote) {
        NOTE_VALIDATOR.validateCreateNote(createNote);
        this.authorId = createNote.authorId();
        this.id = UUID.randomUUID();
        this.title = createNote.title();
        this.content = createNote.content();
        this.created = OffsetDateTime.now();
        this.updated = null;
        this.isPinned = false;
    }

    @Override
    public Optional<OffsetDateTime> getUpdated() {
        return Optional.ofNullable(updated);
    }

    @Override
    public void update(NoteUpdate noteUpdate) {
        NOTE_VALIDATOR.validateNoteUpdate(id, noteUpdate, this);
        boolean updatedAny = false;
        if (noteUpdate.title() != null) {
            this.title = noteUpdate.title();
            updatedAny = true;
        }
        if (noteUpdate.content() != null) {
            this.content = noteUpdate.content();
            updatedAny = true;
        }
        if (noteUpdate.pinned() != null) {
            this.isPinned = noteUpdate.pinned();
            updatedAny = true;
        }
        if (updatedAny) {
            this.updated = OffsetDateTime.now();
        }
    }
}
