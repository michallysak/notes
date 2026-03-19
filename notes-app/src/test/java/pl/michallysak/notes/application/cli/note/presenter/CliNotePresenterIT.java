package pl.michallysak.notes.application.cli.note.presenter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.repository.InMemoryNoteRepository;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.note.service.NoteServiceImpl;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliNotePresenterIT {

    private static final UUID AUTHOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final InMemoryNoteRepository noteRepository = new InMemoryNoteRepository();
    private final NoteService noteService = new NoteServiceImpl(noteRepository);

    @BeforeEach
    void setUp() {
        noteService.getCreatedNotes(AUTHOR_ID).stream()
                .map(NoteValue::id)
                .forEach(noteRepository::deleteById);
        assertTrue(noteService.getCreatedNotes(AUTHOR_ID).isEmpty());
    }

    private CliNotePresenterTestClient createTestClient() {
        return new CliNotePresenterTestClient(noteService);
    }

    @Test
    void present$createNote_shouldCreateNote() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        CliNotePresenterTestClient client = createTestClient();
        // when
        client.createNote(createNote);
        // then
        String output = client.getOutput();
        assertTrue(output.contains("Created:"));
        // and
        assertEquals(1, noteService.getCreatedNotes(AUTHOR_ID).size());
        NoteValue created = noteService.getCreatedNotes(AUTHOR_ID).getFirst();
        assertEquals(createNote.title(), created.title());
        assertEquals(createNote.content(), created.content());
    }

    @Test
    void present$createNote_shouldShowError_whenValidationException() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().title("X").build();
        CliNotePresenterTestClient client = createTestClient();
        // when
        client.createNote(createNote);
        // then
        String output = client.getOutput();
        assertTrue(output.contains("Error: Title not meet length requirements"));
        // and
        assertTrue(noteService.getCreatedNotes(AUTHOR_ID).isEmpty());
    }

    @Test
    void present$listNotes_shouldListNotes_withCreatedNote() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        CliNotePresenterTestClient client = createTestClient();
        client.createNote(createNote);
        // when
        CliNotePresenterTestClient listClient = createTestClient().listNotes();
        // then
        String output = listClient.getOutput();
        assertTrue(output.contains(createNote.title()));
        assertTrue(output.contains(createNote.content()));
        // and
        assertEquals(1, noteService.getCreatedNotes(AUTHOR_ID).size());
    }

    @Test
    void present$listNotes_shouldShowNoNotesFound_whenNoNotes() {
        // when
        CliNotePresenterTestClient client = createTestClient().listNotes();
        // then
        String output = client.getOutput();
        assertTrue(output.contains("No notes found"));
        // and
        assertTrue(noteService.getCreatedNotes(AUTHOR_ID).isEmpty());
    }

    @Test
    void present$getNote_shouldShowNote() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        CliNotePresenterTestClient client = createTestClient();
        String id = client.createNoteAndGetId(createNote);
        // when
        CliNotePresenterTestClient showClient = createTestClient().showNote(id);
        // then
        String output = showClient.getOutput();
        assertTrue(output.contains(createNote.title()));
        assertTrue(output.contains(createNote.content()));
        // and
        NoteValue note = noteService.getCreatedNotes(AUTHOR_ID).getFirst();
        assertEquals(createNote.title(), note.title());
        assertEquals(createNote.content(), note.content());
    }

    @Test
    void present$getNote_shouldShowError_whenNoteNotExists() {
        // given
        String nonExistentId = UUID.randomUUID().toString();
        CliNotePresenterTestClient client = createTestClient().showNote(nonExistentId);
        // then
        String output = client.getOutput();
        assertTrue(output.contains("Note not found"));
        // and
        assertTrue(noteService.getCreatedNotes(AUTHOR_ID).isEmpty());
    }

    @Test
    void present$getNote_shouldShowError_whenInvalidUUID() {
        String notUuid = "not-a-uuid";
        CliNotePresenterTestClient client = new CliNotePresenterTestClient(noteService);
        client.present(NoteOption.SHOW.getValue(), notUuid, NoteOption.EXIT.getValue());
        String output = client.getOutput();
        assertTrue(output.contains("Error: Invalid UUID string"));
        // and
        assertTrue(noteService.getCreatedNotes(AUTHOR_ID).isEmpty());
    }

    @Test
    void present$updateNote_shouldUpdateNote() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        CliNotePresenterTestClient client = createTestClient();
        String id = client.createNoteAndGetId(createNote);
        String newTitle = "newTitle";
        String newContent = "newContent";
        // when
        NoteUpdate noteUpdate = NoteUpdate.builder()
                .title(newTitle)
                .content(newContent)
                .pinned(true)
                .build();
        CliNotePresenterTestClient updateClient = createTestClient().updateNote(id, noteUpdate);
        // then
        String output = updateClient.getOutput();
        assertTrue(output.contains("Updated:"));
        assertTrue(output.contains(newTitle));
        assertTrue(output.contains(newContent));
        // and
        NoteValue updated = noteService.getCreatedNotes(AUTHOR_ID).getFirst();
        assertEquals(newTitle, updated.title());
        assertEquals(newContent, updated.content());
        assertTrue(updated.pinned());
    }

    @Test
    void present$updateNote_shouldShowError_whenUpdateValidationException() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        CliNotePresenterTestClient client = createTestClient();
        String id = client.createNoteAndGetId(createNote);
        String newTitle = "X";
        String newContent = "newContent";
        // when
        NoteUpdate noteUpdate = NoteUpdate.builder()
                .title(newTitle)
                .content(newContent)
                .pinned(true)
                .build();
        CliNotePresenterTestClient updateClient = createTestClient().updateNote(id, noteUpdate);
        // then
        String output = updateClient.getOutput();
        assertTrue(output.contains("Error: Title not meet length requirements"));
        NoteValue note = noteService.getCreatedNotes(AUTHOR_ID).getFirst();
        assertEquals(createNote.title(), note.title());
    }

    @Test
    void present$updateNote_shouldShowNoChangeMessage_whenTitleInputBlanke() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        CliNotePresenterTestClient client = createTestClient();
        String id = client.createNoteAndGetId(createNote);
        // when
        CliNotePresenterTestClient updateClient = createTestClient().present(
            NoteOption.UPDATE.getValue(),
            id,
            "  ",
            "newContent",
            "y",
            NoteOption.EXIT.getValue()
        );
        // then
        String output = updateClient.getOutput();
        assertTrue(output.contains("No change will be applied to title."));
        // and
        NoteValue note = noteService.getCreatedNotes(AUTHOR_ID).getFirst();
        assertEquals(createNote.title(), note.title());
    }

    @Test
    void present$updateNote_shouldShowNoChangeMessage_whenPinnedInputInvalid() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        CliNotePresenterTestClient client = createTestClient();
        String id = client.createNoteAndGetId(createNote);
        // when
        CliNotePresenterTestClient updateClient = createTestClient().present(
            NoteOption.UPDATE.getValue(),
            id,
            "newTitle",
            "newContent",
            "maybe",
            NoteOption.EXIT.getValue()
        );
        // then
        String output = updateClient.getOutput();
        assertTrue(output.contains("No change will be applied to pinned status."));
        // and
        NoteValue note = noteService.getCreatedNotes(AUTHOR_ID).getFirst();
        assertFalse(note.pinned());
    }

    @Test
    void present$deleteNote_shouldDeleteNote() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        CliNotePresenterTestClient client = createTestClient();
        String id = client.createNoteAndGetId(createNote);
        // when
        CliNotePresenterTestClient deleteClient = createTestClient().deleteNote(id);
        // then
        String output = deleteClient.getOutput();
        assertTrue(output.contains("Deleted"));
        // and
        assertTrue(noteService.getCreatedNotes(AUTHOR_ID).isEmpty());
    }

    @Test
    void present$deleteNote_shouldShowError_whenDeleteNotExists() {
        // given
        String nonExistentId = UUID.randomUUID().toString();
        CliNotePresenterTestClient client = createTestClient().deleteNote(nonExistentId);
        // then
        String output = client.getOutput();
        assertTrue(output.contains("Note not found"));
        // and
        assertTrue(noteService.getCreatedNotes(AUTHOR_ID).isEmpty());
    }

}
