package pl.michallysak.notes.note.domain;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;

import static org.junit.jupiter.api.Assertions.*;

class NoteImplTest {
    @Test
    void create_shouldInitializeFieldsCorrectly() {
        // given
        CreateNote createNote = new CreateNote("title", "content");
        // when
        Note note = NoteImpl.create(createNote);
        // then
        assertNotNull(note.getId());
        assertEquals("title", note.getTitle());
        assertEquals("content", note.getContent());
        assertNotNull(note.getCreated());
        assertTrue(note.getUpdated().isEmpty());
        assertFalse(note.isPinned());
    }

    @SneakyThrows
    @Test
    void update_shouldModifyFieldsAndSetUpdated() {
        // given
        CreateNote createNote = new CreateNote("title", "content");
        Note note = NoteImpl.create(createNote);
        NoteUpdate noteUpdate = new NoteUpdate("newTitle", "newContent", true);
        Thread.sleep(100);
        // when
        note.update(noteUpdate);
        // then
        assertEquals("newTitle", note.getTitle());
        assertEquals("newContent", note.getContent());
        assertTrue(note.isPinned());
        assertTrue(note.getUpdated().isPresent());
        assertTrue(note.getUpdated().get().isAfter(note.getCreated()));
    }

}

