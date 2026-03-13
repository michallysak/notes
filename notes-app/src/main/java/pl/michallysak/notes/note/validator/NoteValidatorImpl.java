package pl.michallysak.notes.note.validator;

import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.common.validator.CommonValidator;
import pl.michallysak.notes.common.validator.TextRange;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;

import java.util.UUID;

@RequiredArgsConstructor
public class NoteValidatorImpl implements NoteValidator {

    private final static TextRange TITLE_LENGTH_RANGE = TextRange.of(3, 64);
    private final static TextRange CONTENT_LENGTH_RANGE = TextRange.of(0, 2048);

    private final CommonValidator commonValidator = new CommonValidator();

    @Override
    public void validateCreateNote(CreateNote createNote) throws ValidationException {
        commonValidator.throwOnNull(createNote, "CreateNote cannot be null");
        validateTitle(createNote.title());
        validateContent(createNote.content());
    }

    @Override
    public void validateNoteUpdate(UUID noteId, NoteUpdate noteUpdate) throws ValidationException {
        commonValidator.throwOnNull(noteId, "Note ID cannot be null");
        commonValidator.throwOnNull(noteUpdate, "NoteUpdate cannot be null");
        validateTitle(noteUpdate.title());
        validateContent(noteUpdate.content());
    }

    private void validateTitle(String title) {
        commonValidator.throwOnNull(title, "Title cannot be null");
        commonValidator.throwOnNotInRange(title, TITLE_LENGTH_RANGE, "Title not meet length requirements %s, is %d".formatted(TITLE_LENGTH_RANGE, title.length()));
    }

    private void validateContent(String content) {
        commonValidator.throwOnNull(content, "Content cannot be null");
        commonValidator.throwOnNotInRange(content, CONTENT_LENGTH_RANGE, "Content not meet length requirements %s, is %d".formatted(CONTENT_LENGTH_RANGE, content.length()));
    }

}

