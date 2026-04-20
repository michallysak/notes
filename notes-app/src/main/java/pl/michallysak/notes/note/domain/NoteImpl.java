package pl.michallysak.notes.note.domain;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;
import pl.michallysak.notes.note.exception.NoteAccessException;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.validator.NoteValidator;

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
  private final NoteValidator noteValidator;

  public NoteImpl(CreateNote createNote, NoteValidator noteValidator) {
    this.noteValidator = noteValidator;
    noteValidator.validateCreateNote(createNote);
    this.authorId = createNote.authorId();
    this.id = UUID.randomUUID();
    this.title = createNote.title();
    this.content = createNote.content();
    this.created = OffsetDateTime.now();
    this.updated = null;
    this.isPinned = false;
  }

  public NoteImpl(NoteValue noteValue, NoteValidator noteValidator) {
    this.noteValidator = noteValidator;
    this.id = noteValue.id();
    this.authorId = noteValue.authorId();
    this.title = noteValue.title();
    this.content = noteValue.content();
    this.created = noteValue.created();
    this.updated = noteValue.updated().orElse(null);
    this.isPinned = noteValue.pinned();
  }

  @Override
  public Optional<OffsetDateTime> getUpdated() {
    return Optional.ofNullable(updated);
  }

  @Override
  public void read(UUID actingUserId) {
    checkOwnership(actingUserId);
  }

  @Override
  public void update(NoteUpdate noteUpdate) {
    UUID actingUserId = noteUpdate.actingUserId();
    checkOwnership(actingUserId);
    noteValidator.validateNoteUpdate(id, noteUpdate, this);
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

  @Override
  public void delete(UUID actingUserId) {
    checkOwnership(actingUserId);
  }

  private void checkOwnership(UUID actingUserId) {
    if (!authorId.equals(actingUserId)) {
      throw new NoteAccessException(id, actingUserId);
    }
  }
}
