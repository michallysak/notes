package pl.michallysak.notes.note.validator;

import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;

import java.util.UUID;

public interface NoteValidator {
    void validateCreateNote(CreateNote createNote) throws ValidationException;

    void validateNoteUpdate(UUID noteId, NoteUpdate noteUpdate) throws ValidationException;
}
