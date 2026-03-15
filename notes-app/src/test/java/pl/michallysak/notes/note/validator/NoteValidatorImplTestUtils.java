package pl.michallysak.notes.note.validator;

import pl.michallysak.notes.common.validator.TextRange;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;

import java.util.stream.Stream;

import static pl.michallysak.notes.helpers.TestExtensions.concat;
import static pl.michallysak.notes.helpers.TestExtensions.textsWithLength;

class NoteValidatorImplTestUtils {

    static final TextRange TITLE_LENGTH_RANGE = TextRange.of(3, 64);
    static final TextRange CONTENT_LENGTH_RANGE = TextRange.of(0, 2048);


    static Stream<CreateNote> createNotesCreationWithTitle(Stream<String> list) {
        return list.map(title -> NoteTestUtils.createCreateNoteBuilder().title(title).content("valid content").build());
    }

    static Stream<CreateNote> createNotesCreationWithContent(Stream<String> list) {
        return list.map(content -> NoteTestUtils.createCreateNoteBuilder().title("valid title").content(content).build());
    }

    static Stream<CreateNote> createNotesWithNotInRangeLengthTitle() {
        return createNotesCreationWithTitle(textsWithLength(TITLE_LENGTH_RANGE.getMin() - 1));
    }

    static Stream<CreateNote> createNoteWithNotInRangeLengthContent() {
        return createNotesCreationWithContent(textsWithLength(CONTENT_LENGTH_RANGE.getMax() + 1));
    }

    static Stream<CreateNote> createNotesValid() {
        return concat(
                createNotesCreationWithTitle(textsWithLength(TITLE_LENGTH_RANGE.getMin())),
                createNotesCreationWithTitle(textsWithLength(TITLE_LENGTH_RANGE.getMin())),
                createNotesCreationWithContent(textsWithLength(CONTENT_LENGTH_RANGE.getMin())),
                createNotesCreationWithContent(textsWithLength(CONTENT_LENGTH_RANGE.getMin()))
        );
    }

    static Stream<NoteUpdate> createNoteUpdatesWithTitle(Stream<String> list) {
        return list.map(title -> NoteTestUtils.createNoteUpdateBuilder().title(title).content("valid content").pinned(null).build());
    }

    static Stream<NoteUpdate> createNoteUpdatesWithContent(Stream<String> list) {
        return list.map(content -> NoteTestUtils.createNoteUpdateBuilder().title("valid title").content(content).pinned(null).build());
    }

    static Stream<NoteUpdate> invalidNoteUpdatesWithTitle() {
        return concat(
                createNoteUpdatesWithTitle(textsWithLength(TITLE_LENGTH_RANGE.getMin() - 1)),
                createNoteUpdatesWithTitle(textsWithLength(TITLE_LENGTH_RANGE.getMax() + 1))
        );
    }

    static Stream<NoteUpdate> invalidNoteUpdatesWithContent() {
        return createNoteUpdatesWithContent(textsWithLength(CONTENT_LENGTH_RANGE.getMax() + 1));
    }

    static Stream<NoteUpdate> validNoteUpdates() {
        return concat(
                createNoteUpdatesWithTitle(textsWithLength(TITLE_LENGTH_RANGE.getMin())),
                createNoteUpdatesWithTitle(textsWithLength(TITLE_LENGTH_RANGE.getMax())),
                createNoteUpdatesWithContent(textsWithLength(CONTENT_LENGTH_RANGE.getMin())),
                createNoteUpdatesWithContent(textsWithLength(CONTENT_LENGTH_RANGE.getMax()))
        );
    }

}

