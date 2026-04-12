package pl.michallysak.notes.note.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;

class InMemoryNoteRepositoryTest {

  @Test
  void findAll_shouldReturnEmptyList_whenNoNotes() {
    // given
    NoteRepository noteRepository = createNoteRepository();
    // when
    List<Note> notes = noteRepository.findAll();
    // then
    assertTrue(notes.isEmpty());
  }

  @Test
  void findById_shouldReturnEmpty_whenNoExists() {
    // given
    NoteRepository noteRepository = createNoteRepository();
    UUID randomId = UUID.randomUUID();
    // when
    Optional<Note> note = noteRepository.findById(randomId);
    // then
    assertTrue(note.isEmpty());
  }

  @Test
  void findById_shouldReturnNote_whenExists() {
    // given
    NoteRepository noteRepository = createNoteRepository(createNote());
    UUID randomId = UUID.randomUUID();
    // when
    Optional<Note> note = noteRepository.findById(randomId);
    // then
    assertTrue(note.isEmpty());
  }

  @Test
  void save_shouldPersistenceNote() {
    // given
    NoteRepository noteRepository = createNoteRepository();
    Note note = createNote();
    // when
    noteRepository.save(note);
    // then
    Optional<Note> foundNote = noteRepository.findById(note.getId());
    assertTrue(foundNote.isPresent());
    assertEquals(note.getId(), foundNote.get().getId());
    assertEquals(note.getTitle(), foundNote.get().getTitle());
    assertEquals(note.getContent(), foundNote.get().getContent());
  }

  @Test
  void findAll_shouldReturnAllNotes() {
    // given
    NoteRepository noteRepository = createNoteRepository(createNote(), createNote());
    // when
    List<Note> notes = noteRepository.findAll();
    // then
    assertEquals(2, notes.size());
  }

  @Test
  void delete_shouldRemoveNoteAndReturnTrue_whenExists() {
    // given
    Note note = createNote();
    NoteRepository noteRepository = createNoteRepository(note);
    // when
    boolean deleted = noteRepository.deleteById(note.getId());
    // then
    assertTrue(noteRepository.findById(note.getId()).isEmpty());
    assertTrue(deleted);
  }

  @Test
  void delete_shouldRemoveNoteAndReturnFalse_whenNotExists() {
    // given
    Note note = createNote();
    NoteRepository noteRepository = createNoteRepository(note);
    UUID randomId = UUID.randomUUID();
    // when
    boolean deleted = noteRepository.deleteById(randomId);
    // then
    assertTrue(noteRepository.findById(note.getId()).isPresent());
    assertFalse(deleted);
  }

  @Test
  void update_shouldModifyNote() {
    // given
    Note note = createNote();
    NoteRepository noteRepository = createNoteRepository(note);
    NoteUpdate noteUpdate =
        NoteTestUtils.createNoteUpdateBuilder()
            .title("newT")
            .content("newC")
            .pinned(true)
            .actingUserId(note.getAuthorId())
            .build();
    note.update(noteUpdate);
    // when
    noteRepository.save(note);
    // then
    Note found = noteRepository.findById(note.getId()).orElseThrow();
    assertEquals(noteUpdate.title(), found.getTitle());
    assertEquals(noteUpdate.content(), found.getContent());
    assertTrue(found.isPinned());
  }

  @Test
  void findAllWithAuthor_shouldReturnOnlyNotesWithGivenAuthor() {
    // given
    UUID author1 = UUID.randomUUID();
    UUID author2 = UUID.randomUUID();
    Note note1 = new NoteImpl(NoteTestUtils.createCreateNoteBuilder().authorId(author1).build());
    Note note2 = new NoteImpl(NoteTestUtils.createCreateNoteBuilder().authorId(author2).build());
    Note note3 = new NoteImpl(NoteTestUtils.createCreateNoteBuilder().authorId(author1).build());
    NoteRepository noteRepository = createNoteRepository(note1, note2, note3);
    // when
    List<Note> notes = noteRepository.findAllWithAuthor(author1);
    // then
    assertEquals(2, notes.size());
    assertTrue(notes.stream().allMatch(n -> n.getAuthorId().equals(author1)));
  }

  @Test
  void deleteAll_shouldRemoveAllNotes() {
    // given
    Note note1 = createNote();
    Note note2 = createNote();
    NoteRepository noteRepository = createNoteRepository(note1, note2);
    assertEquals(2, noteRepository.findAll().size());
    // when
    noteRepository.deleteAll();
    // then
    assertTrue(noteRepository.findAll().isEmpty());
  }

  private NoteRepository createNoteRepository(Note... notes) {
    return new InMemoryNoteRepository(Arrays.asList(notes));
  }

  private Note createNote() {
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    return new NoteImpl(createNote);
  }
}
