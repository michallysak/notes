package pl.michallysak.notes.note.validator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pl.michallysak.notes.note.validator.NoteValidatorImplTestUtils.CONTENT_LENGTH_RANGE;
import static pl.michallysak.notes.note.validator.NoteValidatorImplTestUtils.TITLE_LENGTH_RANGE;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;

class NoteValidatorImplTest {

  private final NoteValidatorImpl noteValidator = new NoteValidatorImpl();

  @Test
  void validateCreateNote_shouldThrow_whenNull() {
    // given
    CreateNote createNote = null;
    String message = "CreateNote cannot be null";
    // when
    Executable executable = () -> noteValidator.validateCreateNote(createNote);
    // then
    ValidationException validationException = assertThrows(ValidationException.class, executable);
    assertEquals(message, validationException.getMessage());
  }

  @Test
  void validateCreateNote_shouldThrow_whenNullTitle() {
    // given
    CreateNote noteUpdate = NoteTestUtils.createCreateNoteBuilder().title(null).build();
    String message = "Title cannot be null";
    // when
    Executable executable = () -> noteValidator.validateCreateNote(noteUpdate);
    // then
    ValidationException validationException = assertThrows(ValidationException.class, executable);
    assertEquals(message, validationException.getMessage());
  }

  @ParameterizedTest
  @MethodSource(
      "pl.michallysak.notes.note.validator.NoteValidatorImplTestUtils#createNotesWithNotInRangeLengthTitle")
  void validateCreateNote_shouldThrow_whenNotInRangeLengthTitle(CreateNote note) {
    // given
    String message =
        "Title not meet length requirements %s, is %d"
            .formatted(TITLE_LENGTH_RANGE, note.title().length());
    // when
    Executable executable = () -> noteValidator.validateCreateNote(note);
    // then
    ValidationException validationException = assertThrows(ValidationException.class, executable);
    assertEquals(message, validationException.getMessage());
  }

  @Test
  void validateCreateNote_shouldThrow_whenNullContent() {
    // given
    CreateNote note = NoteTestUtils.createCreateNoteBuilder().content(null).build();
    String message = "Content cannot be null";
    // when
    Executable executable = () -> noteValidator.validateCreateNote(note);
    // then
    ValidationException validationException = assertThrows(ValidationException.class, executable);
    assertEquals(message, validationException.getMessage());
  }

  @ParameterizedTest
  @MethodSource(
      "pl.michallysak.notes.note.validator.NoteValidatorImplTestUtils#createNoteWithNotInRangeLengthContent")
  void validateCreateNote_shouldThrow_whenInvalidContent(CreateNote note) {
    // given
    String message =
        "Content not meet length requirements %s, is %d"
            .formatted(CONTENT_LENGTH_RANGE, note.content().length());
    // when
    Executable executable = () -> noteValidator.validateCreateNote(note);
    // then
    ValidationException validationException = assertThrows(ValidationException.class, executable);
    assertEquals(message, validationException.getMessage());
  }

  @ParameterizedTest
  @MethodSource("pl.michallysak.notes.note.validator.NoteValidatorImplTestUtils#createNotesValid")
  void validateCreateNote_shouldNotThrow_whenValid(CreateNote note) {
    // given
    // valid note from parameter
    // when
    Executable executable = () -> noteValidator.validateCreateNote(note);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void validateCreateNote_shouldThrow_whenNullAuthorId() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().authorId(null).build();
    String message = "AuthorId id cannot be null";
    // when
    Executable executable = () -> noteValidator.validateCreateNote(createNote);
    // then
    ValidationException validationException = assertThrows(ValidationException.class, executable);
    assertEquals(message, validationException.getMessage());
  }

  @Test
  void validateNoteUpdate_shouldThrow_whenNullNoteId() {
    // given
    UUID nullNoteId = null;
    NoteUpdate noteUpdate = NoteTestUtils.createNoteUpdateBuilder().build();
    String message = "Note id cannot be null";
    // when
    Executable executable = () -> noteValidator.validateNoteUpdate(nullNoteId, noteUpdate, null);
    // then
    ValidationException validationException = assertThrows(ValidationException.class, executable);
    assertEquals(message, validationException.getMessage());
  }

  @Test
  void validateNoteUpdate_shouldThrow_whenNullNoteUpdate() {
    // given
    UUID noteId = UUID.randomUUID();
    NoteUpdate noteUpdate = null;
    String message = "NoteUpdate cannot be null";
    // when
    Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate, null);
    // then
    ValidationException validationException = assertThrows(ValidationException.class, executable);
    assertEquals(message, validationException.getMessage());
  }

  @ParameterizedTest
  @MethodSource(
      "pl.michallysak.notes.note.validator.NoteValidatorImplTestUtils#invalidNoteUpdatesWithTitle")
  void validateNoteUpdate_shouldThrow_whenNotInRangeLengthTitle(NoteUpdate noteUpdate) {
    // given
    UUID noteId = UUID.randomUUID();
    String message =
        "Title not meet length requirements %s, is %d"
            .formatted(TITLE_LENGTH_RANGE, noteUpdate.title().length());
    // when
    Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate, null);
    // then
    ValidationException validationException = assertThrows(ValidationException.class, executable);
    assertEquals(message, validationException.getMessage());
  }

  @Test
  void validateNoteUpdate_shouldNotThrow_whenNullTitle() {
    // given
    UUID noteId = UUID.randomUUID();
    NoteUpdate noteUpdate = NoteTestUtils.createNoteUpdateBuilder().title(null).build();
    // when
    Note note = mock(Note.class);
    when(note.isPinned()).thenReturn(false);
    Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate, note);
    // then
    assertDoesNotThrow(executable);
  }

  @ParameterizedTest
  @MethodSource(
      "pl.michallysak.notes.note.validator.NoteValidatorImplTestUtils#invalidNoteUpdatesWithContent")
  void validateNoteUpdate_shouldThrow_whenNotInRangeLengthContent(NoteUpdate noteUpdate) {
    // given
    UUID noteId = UUID.randomUUID();
    String message =
        "Content not meet length requirements %s, is %d"
            .formatted(CONTENT_LENGTH_RANGE, noteUpdate.content().length());
    // when
    Note mock = mock(Note.class);
    when(mock.isPinned()).thenReturn(false);
    Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate, mock);
    // then
    ValidationException validationException = assertThrows(ValidationException.class, executable);
    assertEquals(message, validationException.getMessage());
  }

  @Test
  void validateNoteUpdate_shouldNotThrow_whenNullContent() {
    // given
    UUID noteId = UUID.randomUUID();
    NoteUpdate noteUpdate = NoteTestUtils.createNoteUpdateBuilder().content(null).build();
    // when
    Note note = mock(Note.class);
    Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate, note);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void validateNoteUpdate_shouldThrow_whenAlreadyPinned() {
    // given
    String message = "Note is already pinned";
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID noteId = note.getId();
    // and
    NoteUpdate noteUpdate =
        NoteTestUtils.createNoteUpdateBuilder()
            .pinned(true)
            .actingUserId(note.getAuthorId())
            .build();
    note.update(noteUpdate);
    // when
    Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate, note);
    // then
    ValidationException validationException = assertThrows(ValidationException.class, executable);
    assertEquals(message, validationException.getMessage());
  }

  @Test
  void validateNoteUpdate_shouldThrow_whenAlreadyUnpinned() {
    // given
    String message = "Note is already unpinned";
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID noteId = note.getId();
    // and
    NoteUpdate noteUpdate =
        NoteTestUtils.createNoteUpdateBuilder()
            .pinned(false)
            .actingUserId(note.getAuthorId())
            .build();
    // when
    Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate, note);
    // then
    ValidationException validationException = assertThrows(ValidationException.class, executable);
    assertEquals(message, validationException.getMessage());
  }

  @Test
  void validateNoteUpdate_shouldNotThrow_whenPinStateChanges() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID noteId = note.getId();
    // and
    NoteUpdate noteUpdate = NoteTestUtils.createNoteUpdateBuilder().pinned(true).build();
    // when
    Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate, note);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void validateNoteUpdate_shouldNotThrow_whenUnpinStateChanges() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID noteId = note.getId();
    // and
    NoteUpdate noteUpdate =
        NoteTestUtils.createNoteUpdateBuilder()
            .pinned(true)
            .actingUserId(note.getAuthorId())
            .build();
    note.update(noteUpdate);
    NoteUpdate noteUpdateSecond =
        NoteTestUtils.createNoteUpdateBuilder()
            .pinned(false)
            .actingUserId(note.getAuthorId())
            .build();
    // when
    Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdateSecond, note);
    // then
    assertDoesNotThrow(executable);
  }

  @ParameterizedTest
  @MethodSource("pl.michallysak.notes.note.validator.NoteValidatorImplTestUtils#validNoteUpdates")
  void validateNoteUpdate_shouldNotThrow_whenValid(NoteUpdate noteUpdate) {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID noteId = note.getId();
    // when
    Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate, note);
    // then
    assertDoesNotThrow(executable);
  }
}
