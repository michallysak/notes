package pl.michallysak.notes.note.domain;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.exception.NoteAccessException;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NoteImplTest {
    @Test
    void create_shouldInitializeFieldsCorrectly() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        // when
        Note note = new NoteImpl(createNote);
        // then
        assertNotNull(note.getId());
        assertEquals(createNote.authorId(), note.getAuthorId());
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
        Note note = new NoteImpl(createNote);
        NoteUpdate noteUpdate = NoteTestUtils.createNoteUpdateBuilder()
                .title("newTitle")
                .content("newContent")
                .pinned(true)
                .actingUserId(note.getAuthorId())
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
        Note note = new NoteImpl(createNote);
        NoteUpdate noteUpdate = NoteTestUtils.createNoteUpdateBuilder()
                .title("newTitle")
                .content("newContent")
                .actingUserId(note.getAuthorId())
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
        Note note = new NoteImpl(createNote);
        Thread.sleep(100);
        NoteUpdate noteUpdate = NoteUpdate.builder().actingUserId(note.getAuthorId()).build();
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
        Note note = new NoteImpl(createNote);
        Thread.sleep(100);
        NoteUpdate noteUpdate = NoteUpdate.builder()
                .content("newContent")
                .pinned(true)
                .actingUserId(note.getAuthorId())
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
        Note note = new NoteImpl(createNote);
        Thread.sleep(100);
        NoteUpdate noteUpdate = NoteUpdate.builder()
                .title("newTitle")
                .pinned(true)
                .actingUserId(note.getAuthorId())
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
        Note note = new NoteImpl(createNote);
        Thread.sleep(100);
        NoteUpdate noteUpdate = NoteUpdate.builder()
                .title("newTitle")
                .content("newContent")
                .actingUserId(note.getAuthorId())
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

    @Test
    void update_shouldThrowNoteAccessException_whenUserIsNotAuthor() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        Note note = new NoteImpl(createNote);
        UUID notAuthorId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        NoteUpdate noteUpdate = NoteUpdate.builder()
                .title("newTitle")
                .content("newContent")
                .pinned(true)
                .actingUserId(notAuthorId)
                .build();
        // when
        Executable executable = () -> note.update(noteUpdate);
        // then
        assertThrows(NoteAccessException.class, executable);
    }

    @Test
    void delete_shouldThrowNoteAccessException_whenUserIsNotAuthor() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        Note note = new NoteImpl(createNote);
        UUID notAuthorId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        // when
        Executable executable = () -> note.delete(notAuthorId);
        // then
        assertThrows(NoteAccessException.class, executable);
    }

    @Test
    void read_shouldThrowNoteAccessException_whenUserIsNotAuthor() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        Note note = new NoteImpl(createNote);
        UUID notAuthorId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        // when
        Executable executable = () -> note.read(notAuthorId);
        // then
        assertThrows(NoteAccessException.class, executable);
    }
}
