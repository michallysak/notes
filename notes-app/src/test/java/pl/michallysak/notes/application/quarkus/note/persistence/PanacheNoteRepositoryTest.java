package pl.michallysak.notes.application.quarkus.note.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.note.mapper.NoteMapper;
import pl.michallysak.notes.note.domain.Note;

@ExtendWith(MockitoExtension.class)
class PanacheNoteRepositoryTest {

  @Mock private NoteMapper noteMapper;
  @Mock private EntityManager entityManager;

  private PanacheNoteRepository noteRepository;

  @BeforeEach
  void setup() {
    noteRepository = spy(new PanacheNoteRepository(noteMapper));
  }

  @Test
  void findNotes_shouldReturnEmptyList_whenNoNotes() {
    // given
    doReturn(List.of()).when(noteRepository).listAll();
    // when
    List<Note> notes = noteRepository.findNotes();
    // then
    assertTrue(notes.isEmpty());
  }

  @Test
  void findNoteWithId_shouldReturnEmpty_whenNoExists() {
    // given
    UUID randomId = UUID.randomUUID();
    doReturn(null).when(noteRepository).findById(randomId);
    // when
    Optional<Note> note = noteRepository.findNoteWithId(randomId);
    // then
    assertTrue(note.isEmpty());
  }

  @Test
  void findNoteWithId_shouldReturnNote_whenExists() {
    // given
    UUID id = UUID.randomUUID();
    NoteEntity noteEntity = new NoteEntity();
    Note mappedNote = mock(Note.class);
    doReturn(noteEntity).when(noteRepository).findById(id);
    when(noteMapper.mapToDomain(noteEntity)).thenReturn(mappedNote);
    // when
    Optional<Note> note = noteRepository.findNoteWithId(id);
    // then
    assertTrue(note.isPresent());
    assertEquals(mappedNote, note.orElseThrow());
  }

  @Test
  void save_Note_shouldPersistenceNote() {
    // given
    Note note = mock(Note.class);
    NoteEntity noteEntity = new NoteEntity();
    when(noteMapper.mapToEntity(note)).thenReturn(noteEntity);
    doReturn(entityManager).when(noteRepository).getEntityManager();
    when(entityManager.merge(noteEntity)).thenReturn(noteEntity);
    // when
    noteRepository.saveNote(note);
    // then
    verify(noteMapper).mapToEntity(note);
    verify(entityManager).merge(noteEntity);
  }

  @Test
  void findAll_shouldReturnNotesNotes() {
    // given
    NoteEntity entity1 = new NoteEntity();
    NoteEntity entity2 = new NoteEntity();
    Note note1 = mock(Note.class);
    Note note2 = mock(Note.class);
    doReturn(List.of(entity1, entity2)).when(noteRepository).listAll();
    when(noteMapper.mapToDomain(entity1)).thenReturn(note1);
    when(noteMapper.mapToDomain(entity2)).thenReturn(note2);
    // when
    List<Note> notes = noteRepository.findNotes();
    // then
    assertEquals(2, notes.size());
    assertEquals(List.of(note1, note2), notes);
  }

  @Test
  void delete_shouldRemoveNoteAndReturnTrue_whenExists() {
    // given
    UUID id = UUID.randomUUID();
    doReturn(true).when(noteRepository).deleteById(id);
    // when
    boolean deleted = noteRepository.deleteNoteWithId(id);
    // then
    assertTrue(deleted);
  }

  @Test
  void delete_shouldRemoveNoteAndReturnFalse_whenNotExists() {
    // given
    UUID id = UUID.randomUUID();
    doReturn(false).when(noteRepository).deleteById(id);
    // when
    boolean deleted = noteRepository.deleteNoteWithId(id);
    // then
    assertFalse(deleted);
  }

  @Test
  void update_shouldModifyNote() {
    // given
    Note note = mock(Note.class);
    NoteEntity noteEntity = new NoteEntity();
    when(noteMapper.mapToEntity(note)).thenReturn(noteEntity);
    doReturn(entityManager).when(noteRepository).getEntityManager();
    when(entityManager.merge(noteEntity)).thenReturn(noteEntity);
    // when
    noteRepository.saveNote(note);
    noteRepository.saveNote(note);
    // then
    verify(entityManager, times(2)).merge(noteEntity);
  }

  @Test
  void findNotesWithAuthor_shouldReturnOnlyNotesWithGivenAuthor() {
    // given
    UUID authorId = UUID.randomUUID();
    NoteEntity entity1 = new NoteEntity();
    NoteEntity entity2 = new NoteEntity();
    Note note1 = mock(Note.class);
    Note note2 = mock(Note.class);
    doReturn(List.of(entity1, entity2)).when(noteRepository).list("authorId", authorId);
    when(noteMapper.mapToDomain(entity1)).thenReturn(note1);
    when(noteMapper.mapToDomain(entity2)).thenReturn(note2);
    // when
    List<Note> notes = noteRepository.findNotesWithAuthor(authorId);
    // then
    assertEquals(2, notes.size());
    assertEquals(List.of(note1, note2), notes);
  }

  @Test
  void deleteAll_shouldRemoveNotesNotes() {
    // given
    doReturn(0L).when(noteRepository).deleteAll();
    // when
    noteRepository.deleteNotes();
    // then
    verify(noteRepository).deleteAll();
  }
}
