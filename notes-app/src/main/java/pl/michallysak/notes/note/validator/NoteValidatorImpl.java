package pl.michallysak.notes.note.validator;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.common.validator.CommonValidator;
import pl.michallysak.notes.common.validator.LongRange;
import pl.michallysak.notes.common.validator.TextRange;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.FieldSort;
import pl.michallysak.notes.note.model.NotePagedQuery;
import pl.michallysak.notes.note.model.NoteUpdate;

@RequiredArgsConstructor
public class NoteValidatorImpl implements NoteValidator {

  private static final TextRange TITLE_LENGTH_RANGE = TextRange.of(3, 64);
  private static final TextRange CONTENT_LENGTH_RANGE = TextRange.of(0, 2048);
  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("created", "updated", "title", "pinned");
  private static final LongRange SEARCH_PAGE_RANGE = LongRange.of(0L, Long.MAX_VALUE);
  private static final LongRange SEARCH_SIZE_RANGE = LongRange.of(1L, 100L);

  private final CommonValidator commonValidator = new CommonValidator();

  @Override
  public void validateCreateNote(CreateNote createNote) throws ValidationException {
    commonValidator.throwOnNull(createNote, "CreateNote cannot be null");
    commonValidator.throwOnNull(createNote.authorId(), "AuthorId id cannot be null");
    validateTitle(createNote.title());
    validateContent(createNote.content());
  }

  @Override
  public void validateNoteQuery(NotePagedQuery query) throws ValidationException {
    commonValidator.throwOnNull(query, "NoteQuery cannot be null");
    commonValidator.throwOnNotInRange(
        query.getSize(),
        SEARCH_SIZE_RANGE,
        "Size must be in range %s".formatted(SEARCH_SIZE_RANGE));
    commonValidator.throwOnNotInRange(
        query.getPage(),
        SEARCH_PAGE_RANGE,
        "Page must be in range %s".formatted(SEARCH_PAGE_RANGE));
    List<FieldSort> sort = query.getSort();
    if (sort == null) {
      return;
    }
    for (FieldSort fieldSort : sort) {
      if (!ALLOWED_SORT_FIELDS.contains(fieldSort.field())) {
        throw new ValidationException("Invalid sort field: " + fieldSort.field());
      }
    }
  }

  @Override
  public void validateNoteUpdate(UUID noteId, NoteUpdate noteUpdate, Note note)
      throws ValidationException {
    commonValidator.throwOnNull(noteId, "Note id cannot be null");
    commonValidator.throwOnNull(noteUpdate, "NoteUpdate cannot be null");
    if (noteUpdate.title() != null) {
      validateTitle(noteUpdate.title());
    }
    if (noteUpdate.content() != null) {
      validateContent(noteUpdate.content());
    }
    validatePinned(noteUpdate.pinned(), note);
  }

  private void validateTitle(String title) {
    commonValidator.throwOnNull(title, "Title cannot be null");
    commonValidator.throwOnNotInRange(
        title,
        TITLE_LENGTH_RANGE,
        "Title not meet length requirements %s, is %d"
            .formatted(TITLE_LENGTH_RANGE, title.length()));
  }

  private void validateContent(String content) {
    commonValidator.throwOnNull(content, "Content cannot be null");
    commonValidator.throwOnNotInRange(
        content,
        CONTENT_LENGTH_RANGE,
        "Content not meet length requirements %s, is %d"
            .formatted(CONTENT_LENGTH_RANGE, content.length()));
  }

  private void validatePinned(Boolean pinned, Note note) {
    Objects.requireNonNull(note, "Note cannot be null");
    if (pinned == null) {
      return;
    }
    boolean currentPinned = note.isPinned();
    if (pinned && currentPinned) {
      throw new ValidationException("Note is already pinned");
    }
    if (!pinned && !currentPinned) {
      throw new ValidationException("Note is already unpinned");
    }
  }
}
