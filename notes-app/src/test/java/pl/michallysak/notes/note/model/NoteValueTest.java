package pl.michallysak.notes.note.model;

import org.junit.jupiter.api.Test;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoteValueTest {

    @Test
    void from_shouldMapNoteFieldsCorrectly() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        Note note = new NoteImpl(createNote);
        // when
        NoteValue value = NoteValue.from(note);
        // then
        assertEquals(note.getId(), value.id());
        assertEquals(note.getAuthorId(), value.authorId());
        assertEquals(note.getTitle(), value.title());
        assertEquals(note.getContent(), value.content());
        assertEquals(note.getCreated(), value.created());
        assertEquals(note.getUpdated(), value.updated());
        assertEquals(note.isPinned(), value.pinned());
    }
}
