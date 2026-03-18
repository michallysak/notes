package pl.michallysak.notes.note.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;
import pl.michallysak.notes.note.exception.NoteNotFoundException;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.repository.NoteRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteRepository repository;

    @InjectMocks
    private NoteServiceImpl service;

    @Test
    void createNote_shouldValidateAndSave() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        // when
        NoteValue noteValue = service.createNote(createNote);
        // then
        verify(repository).save(any());

        assertEquals(createNote.title(), noteValue.title());
        assertEquals(createNote.content(), noteValue.content());
    }

    @Test
    void getCreatedNotes_shouldReturnMappedValues() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        Note note = new NoteImpl(createNote);
        when(repository.findAll()).thenReturn(List.of(note));
        // when
        List<NoteValue> noteValues = service.getCreatedNotes();
        // then
        assertEquals(1, noteValues.size());
        assertEquals(NoteValue.from(note), noteValues.getFirst());
    }

    @Test
    void getCreatedNote_shouldReturnMappedValue() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        Note note = new NoteImpl(createNote);
        UUID id = note.getId();
        when(repository.findById(id)).thenReturn(Optional.of(note));
        // when
        NoteValue noteValue = service.getCreatedNote(id);
        // then
        assertEquals(NoteValue.from(note), noteValue);
    }

    @Test
    void getCreatedNote_shouldThrow_whenNotExists() {
        // given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        // when
        Executable executable = () -> service.getCreatedNote(id);
        // then
        assertThrows(NoteNotFoundException.class, executable);
    }

    @Test
    void updateNote_shouldValidateAndSave() {
        // given
        CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
        Note note = new NoteImpl(createNote);
        UUID id = note.getId();
        NoteUpdate update = NoteTestUtils.createNoteUpdateBuilder().pinned(null).build();
        when(repository.findById(id)).thenReturn(Optional.of(note));
        // when
        NoteValue noteValue = service.updateNote(id, update);
        // then
        verify(repository).save(note);
        assertEquals(NoteValue.from(note), noteValue);
    }

    @Test
    void deleteNote_shouldDelete() {
        // given
        UUID id = UUID.randomUUID();
        when(repository.deleteById(id)).thenReturn(true);
        // when
        service.deleteNote(id);
        // then
        verify(repository).deleteById(id);
    }

    @Test
    void deleteNote_shouldThrow_whenNotExists() {
        // given
        UUID id = UUID.randomUUID();
        // when
        when(repository.deleteById(id)).thenReturn(false);
        Executable executable = () -> service.deleteNote(id);
        //then
        assertThrows(NoteNotFoundException.class, executable);
    }

}
