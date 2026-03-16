package pl.michallysak.notes.note.domain;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;

import static org.junit.jupiter.api.Assertions.*;

class NoteImplTest {
    @Test
    void create_shouldInitializeFieldsCorrectly() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        // when
        Note note = NoteImpl.create(createNote);
        // then
        assertNotNull(note.getId());
        assertEquals(createNote.title(), note.getTitle());
        assertEquals(createNote.content(), note.getContent());
        assertNotNull(note.getCreated());
        assertTrue(note.getUpdated().isEmpty());
        assertFalse(note.isPinned());
    }

    @SneakyThrows
    @Test
    void update_shouldModifyFieldsAndSetUpdated_whenNotNullPinned() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        Note note = NoteImpl.create(createNote);
        NoteUpdate noteUpdate = NoteTestUtils.createNoteUpdateBuilder()
                .title("newTitle")
                .content("newContent")
                .pinned(true)
                .build();
        Thread.sleep(100);
        // when
        note.update(noteUpdate);
        // then
        assertEquals(noteUpdate.title(), note.getTitle());
        assertEquals(noteUpdate.content(), note.getContent());
        assertEquals(noteUpdate.pinned(), note.isPinned());
        assertTrue(note.getUpdated().isPresent());
        assertTrue(note.getUpdated().get().isAfter(note.getCreated()));
    }

    @SneakyThrows
    @Test
    void update_shouldModifyFieldsAndSetUpdated_whenNullPinned() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        Note note = NoteImpl.create(createNote);
        NoteUpdate noteUpdate = NoteTestUtils.createNoteUpdateBuilder()
                .title("newTitle")
                .content("newContent")
                .build();
        Thread.sleep(100);
        // when
        note.update(noteUpdate);
        // then
        assertEquals(noteUpdate.title(), note.getTitle());
        assertEquals(noteUpdate.content(), note.getContent());
        assertEquals(note.isPinned(), note.isPinned());
        assertTrue(note.getUpdated().isPresent());
        assertTrue(note.getUpdated().get().isAfter(note.getCreated()));
    }

    @SneakyThrows
    @Test
    void update_shouldNotModifyFieldsOrSetUpdated_whenAllFieldsNull() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        Note note = NoteImpl.create(createNote);
        Thread.sleep(100);
        NoteUpdate noteUpdate = NoteUpdate.builder().build();
        // when
        note.update(noteUpdate);
        // then
        assertEquals(createNote.title(), note.getTitle());
        assertEquals(createNote.content(), note.getContent());
        assertFalse(note.isPinned());
        assertTrue(note.getUpdated().isEmpty());
    }

    @SneakyThrows
    @Test
    void update_shouldNotModifyTitle_whenTitleIsNull() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        Note note = NoteImpl.create(createNote);
        Thread.sleep(100);
        NoteUpdate noteUpdate = NoteUpdate.builder()
                .content("newContent")
                .pinned(true)
                .build();
        // when
        note.update(noteUpdate);
        // then
        assertEquals(createNote.title(), note.getTitle());
        assertEquals(noteUpdate.content(), note.getContent());
        assertTrue(note.isPinned());
        assertTrue(note.getUpdated().isPresent());
        assertTrue(note.getUpdated().get().isAfter(note.getCreated()));
    }

    @SneakyThrows
    @Test
    void update_shouldNotModifyContent_whenContentIsNull() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        Note note = NoteImpl.create(createNote);
        Thread.sleep(100);
        NoteUpdate noteUpdate = NoteUpdate.builder()
                .title("newTitle")
                .pinned(true)
                .build();
        // when
        note.update(noteUpdate);
        // then
        assertEquals(noteUpdate.title(), note.getTitle());
        assertEquals(createNote.content(), note.getContent());
        assertTrue(note.isPinned());
        assertTrue(note.getUpdated().isPresent());
        assertTrue(note.getUpdated().get().isAfter(note.getCreated()));
    }

    @SneakyThrows
    @Test
    void update_shouldNotModifyPinned_whenPinnedIsNull() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        Note note = NoteImpl.create(createNote);
        Thread.sleep(100);
        NoteUpdate noteUpdate = NoteUpdate.builder()
                .title("newTitle")
                .content("newContent")
                .build();
        // when
        note.update(noteUpdate);
        // then
        assertEquals(noteUpdate.title(), note.getTitle());
        assertEquals(noteUpdate.content(), note.getContent());
        assertFalse(note.isPinned());
        assertTrue(note.getUpdated().isPresent());
        assertTrue(note.getUpdated().get().isAfter(note.getCreated()));
    }
}
