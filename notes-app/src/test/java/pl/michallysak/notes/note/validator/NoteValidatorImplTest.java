package pl.michallysak.notes.note.validator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.common.validator.TextRange;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.service.NoteTestUtils;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pl.michallysak.notes.helpers.TestExtensions.concat;
import static pl.michallysak.notes.helpers.TestExtensions.textsWithLength;

class NoteValidatorImplTest {

    static final TextRange TITLE_LENGTH_RANGE = TextRange.of(3, 64);
    static final TextRange CONTENT_LENGTH_RANGE = TextRange.of(0, 2048);

    private final NoteValidatorImpl noteValidator = new NoteValidatorImpl();

    private static Stream<CreateNote> createNotesCreationWithTitle(Stream<String> list) {
        return list.map(title -> NoteTestUtils.createCreateNoteBuilder().title(title).content("valid content").build());
    }

    private static Stream<CreateNote> createNotesCreationWithContent(Stream<String> list) {
        return list.map(content -> NoteTestUtils.createCreateNoteBuilder().title("valid title").content(content).build());
    }

    private static Stream<CreateNote> createNotesWithNotInRangeLengthTitle() {
        return createNotesCreationWithTitle(textsWithLength(TITLE_LENGTH_RANGE.getMin() - 1));
    }

    private static Stream<CreateNote> createNoteWithNotInRangeLengthContent() {
        return createNotesCreationWithContent(textsWithLength(CONTENT_LENGTH_RANGE.getMax() + 1));
    }

    private static Stream<CreateNote> createNotesValid() {
        return concat(
                createNotesCreationWithTitle(textsWithLength(TITLE_LENGTH_RANGE.getMin())),
                createNotesCreationWithTitle(textsWithLength(TITLE_LENGTH_RANGE.getMin())),
                createNotesCreationWithContent(textsWithLength(CONTENT_LENGTH_RANGE.getMin())),
                createNotesCreationWithContent(textsWithLength(CONTENT_LENGTH_RANGE.getMin()))
        );
    }

    private static Stream<NoteUpdate> createNoteUpdatesWithTitle(Stream<String> list) {
        return list.map(title -> NoteTestUtils.createNoteUpdateBuilder().title(title).content("valid content").build());
    }

    private static Stream<NoteUpdate> createNoteUpdatesWithContent(Stream<String> list) {
        return list.map(content -> NoteTestUtils.createNoteUpdateBuilder().title("valid title").content(content).build());
    }

    private static Stream<NoteUpdate> invalidNoteUpdatesWithTitle() {
        return concat(
                createNoteUpdatesWithTitle(textsWithLength(TITLE_LENGTH_RANGE.getMin() - 1)),
                createNoteUpdatesWithTitle(textsWithLength(TITLE_LENGTH_RANGE.getMax() + 1))
        );
    }

    private static Stream<NoteUpdate> invalidNoteUpdatesWithContent() {
        return createNoteUpdatesWithContent(textsWithLength(CONTENT_LENGTH_RANGE.getMax() + 1));
    }

    private static Stream<NoteUpdate> validNoteUpdates() {
        return concat(
                createNoteUpdatesWithTitle(textsWithLength(TITLE_LENGTH_RANGE.getMin())),
                createNoteUpdatesWithTitle(textsWithLength(TITLE_LENGTH_RANGE.getMax())),
                createNoteUpdatesWithContent(textsWithLength(CONTENT_LENGTH_RANGE.getMin())),
                createNoteUpdatesWithContent(textsWithLength(CONTENT_LENGTH_RANGE.getMax()))
        );
    }

    @Test
    void validateCreateNote_shouldThrow_whenNull() {
        // given
        CreateNote createNote = null;
        String message = "CreateNote cannot be null";
        // when
        Executable executable = () -> noteValidator.validateCreateNote(createNote);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        Assertions.assertEquals(message, validationException.getMessage());
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
        Assertions.assertEquals(message, validationException.getMessage());
    }

    @ParameterizedTest
    @MethodSource("createNotesWithNotInRangeLengthTitle")
    void validateCreateNote_shouldThrow_whenNotInRangeLengthTitle(CreateNote note) {
        // given
        String message = "Title not meet length requirements %s, is %d".formatted(TITLE_LENGTH_RANGE, note.title().length());
        // when
        Executable executable = () -> noteValidator.validateCreateNote(note);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        Assertions.assertEquals(message, validationException.getMessage());
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
        Assertions.assertEquals(message, validationException.getMessage());
    }

    @ParameterizedTest
    @MethodSource("createNoteWithNotInRangeLengthContent")
    void validateCreateNote_shouldThrow_whenInvalidContent(CreateNote note) {
        // given
        String message = "Content not meet length requirements %s, is %d".formatted(CONTENT_LENGTH_RANGE, note.content().length());
        // when
        Executable executable = () -> noteValidator.validateCreateNote(note);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        Assertions.assertEquals(message, validationException.getMessage());
    }

    @ParameterizedTest
    @MethodSource("createNotesValid")
    void validateCreateNote_shouldNotThrow_whenValid(CreateNote note) {
        // given
        // valid note from parameter
        // when
        Executable executable = () -> noteValidator.validateCreateNote(note);
        // then
        assertDoesNotThrow(executable);
    }

    @Test
    void validateNoteUpdate_shouldThrow_whenNullNoteId() {
        // given
        UUID nullNoteId = null;
        NoteUpdate noteUpdate = NoteTestUtils.createNoteUpdateBuilder().build();
        String message = "Note ID cannot be null";
        // when
        Executable executable = () -> noteValidator.validateNoteUpdate(nullNoteId, noteUpdate);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        Assertions.assertEquals(message, validationException.getMessage());
    }

    @Test
    void validateNoteUpdate_shouldThrow_whenNullNoteUpdate() {
        // given
        UUID noteId = UUID.randomUUID();
        NoteUpdate noteUpdate = null;
        String message = "NoteUpdate cannot be null";
        // when
        Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        Assertions.assertEquals(message, validationException.getMessage());
    }

    @ParameterizedTest
    @MethodSource("invalidNoteUpdatesWithTitle")
    void validateNoteUpdate_shouldThrow_whenNotInRangeLengthTitle(NoteUpdate noteUpdate) {
        // given
        UUID noteId = UUID.randomUUID();
        String message = "Title not meet length requirements %s, is %d".formatted(TITLE_LENGTH_RANGE, noteUpdate.title().length());
        // when
        Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        Assertions.assertEquals(message, validationException.getMessage());
    }

    @Test
    void validateNoteUpdate_shouldThrow_whenNullTitle() {
        // given
        UUID noteId = UUID.randomUUID();
        NoteUpdate noteUpdate = NoteTestUtils.createNoteUpdateBuilder().title(null).build();
        String message = "Title cannot be null";
        // when
        Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        Assertions.assertEquals(message, validationException.getMessage());
    }

    @ParameterizedTest
    @MethodSource("invalidNoteUpdatesWithContent")
    void validateNoteUpdate_shouldThrow_whenNotInRangeLengthContent(NoteUpdate noteUpdate) {
        // given
        UUID noteId = UUID.randomUUID();
        String message = "Content not meet length requirements %s, is %d".formatted(CONTENT_LENGTH_RANGE, noteUpdate.content().length());
        // when
        Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        Assertions.assertEquals(message, validationException.getMessage());
    }

    @Test
    void validateNoteUpdate_shouldThrow_whenNullContent() {
        // given
        UUID noteId = UUID.randomUUID();
        NoteUpdate noteUpdate = NoteTestUtils.createNoteUpdateBuilder().content(null).build();
        String message = "Content cannot be null";
        // when
        Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        Assertions.assertEquals(message, validationException.getMessage());
    }

    @ParameterizedTest
    @MethodSource("validNoteUpdates")
    void validateNoteUpdate_shouldNotThrow_whenValid(NoteUpdate noteUpdate) {
        // given
        UUID noteId = UUID.randomUUID();
        // when
        Executable executable = () -> noteValidator.validateNoteUpdate(noteId, noteUpdate);
        // then
        assertDoesNotThrow(executable);
    }

}
