package pl.michallysak.notes.note.validator;

import java.util.UUID;
import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;

public interface NoteValidator {
  void validateCreateNote(CreateNote createNote) throws ValidationException;

  void validateNoteUpdate(UUID noteId, NoteUpdate noteUpdate, Note note) throws ValidationException;
}
