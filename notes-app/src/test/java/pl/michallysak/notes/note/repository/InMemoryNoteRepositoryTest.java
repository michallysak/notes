package pl.michallysak.notes.note.repository;

import static org.junit.jupiter.api.Assertions.*;
import static pl.michallysak.notes.note.NoteTestUtils.createNotePagedQuery;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;
import pl.michallysak.notes.note.model.*;
import pl.michallysak.notes.note.model.Paged;
import pl.michallysak.notes.note.validator.NoteValidator;

@ExtendWith(MockitoExtension.class)
class InMemoryNoteRepositoryTest {

  @Mock private NoteValidator noteValidator;

  @Test
  void findNotes_shouldReturnEmptyList_whenNoNotes() {
    // given
    NoteRepository noteRepository = createNoteRepository();
    // when
    List<Note> notes = noteRepository.findNotes();
    // then
    assertTrue(notes.isEmpty());
  }

  @Test
  void findNoteWithId_shouldReturnEmpty_whenNoExists() {
    // given
    NoteRepository noteRepository = createNoteRepository();
    UUID randomId = UUID.randomUUID();
    // when
    Optional<Note> note = noteRepository.findNoteWithId(randomId);
    // then
    assertTrue(note.isEmpty());
  }

  @Test
  void findNoteWithId_shouldReturnNote_whenExists() {
    // given
    NoteRepository noteRepository = createNoteRepository(createNote());
    UUID randomId = UUID.randomUUID();
    // when
    Optional<Note> note = noteRepository.findNoteWithId(randomId);
    // then
    assertTrue(note.isEmpty());
  }

  @Test
  void save_Note_shouldPersistenceNote() {
    // given
    NoteRepository noteRepository = createNoteRepository();
    Note note = createNote();
    // when
    noteRepository.saveNote(note);
    // then
    Optional<Note> foundNote = noteRepository.findNoteWithId(note.getId());
    assertTrue(foundNote.isPresent());
    assertEquals(note.getId(), foundNote.get().getId());
    assertEquals(note.getTitle(), foundNote.get().getTitle());
    assertEquals(note.getContent(), foundNote.get().getContent());
  }

  @Test
  void findAll_shouldReturnNotesNotes() {
    // given
    NoteRepository noteRepository = createNoteRepository(createNote(), createNote());
    // when
    List<Note> notes = noteRepository.findNotes();
    // then
    assertEquals(2, notes.size());
  }

  @Test
  void delete_shouldRemoveNoteAndReturnTrue_whenExists() {
    // given
    Note note = createNote();
    NoteRepository noteRepository = createNoteRepository(note);
    // when
    boolean deleted = noteRepository.deleteNoteWithId(note.getId());
    // then
    assertTrue(noteRepository.findNoteWithId(note.getId()).isEmpty());
    assertTrue(deleted);
  }

  @Test
  void delete_shouldRemoveNoteAndReturnFalse_whenNotExists() {
    // given
    Note note = createNote();
    NoteRepository noteRepository = createNoteRepository(note);
    UUID randomId = UUID.randomUUID();
    // when
    boolean deleted = noteRepository.deleteNoteWithId(randomId);
    // then
    assertTrue(noteRepository.findNoteWithId(note.getId()).isPresent());
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
    noteRepository.saveNote(note);
    // then
    Note found = noteRepository.findNoteWithId(note.getId()).orElseThrow();
    assertEquals(noteUpdate.title(), found.getTitle());
    assertEquals(noteUpdate.content(), found.getContent());
    assertTrue(found.isPinned());
  }

  @Test
  void findNotesWithAuthor_shouldReturnOnlyNotesWithGivenAuthor() {
    // given
    UUID author1 = UUID.randomUUID();
    UUID author2 = UUID.randomUUID();
    CreateNote createNote1 = NoteTestUtils.createCreateNoteBuilder().authorId(author1).build();
    CreateNote createNote2 = NoteTestUtils.createCreateNoteBuilder().authorId(author2).build();
    Note note1 = new NoteImpl(createNote1, noteValidator);
    Note note2 = new NoteImpl(createNote2, noteValidator);
    Note note3 = new NoteImpl(createNote1, noteValidator);
    NoteRepository noteRepository = createNoteRepository(note1, note2, note3);
    // when
    List<Note> notes = noteRepository.findNotesWithAuthor(author1);
    // then
    assertEquals(2, notes.size());
    assertTrue(notes.stream().allMatch(n -> n.getAuthorId().equals(author1)));
  }

  @Test
  void search_shouldReturnPagedNotes_whenIsSharedNull() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    CreateNote createShared = NoteTestUtils.createCreateNoteBuilder().authorId(authorId).build();
    CreateNote createPrivate = NoteTestUtils.createCreateNoteBuilder().authorId(authorId).build();
    Note shared = new NoteImpl(createShared, noteValidator);
    shared.setPermissions(authorId, targetUserId, Set.of(NotePermission.READ));
    Note privateNote = new NoteImpl(createPrivate, noteValidator);
    NoteRepository noteRepository = createNoteRepository(shared, privateNote);
    NotePagedQuery query = createNotePagedQuery(null, null, 0, 1, null);
    // when
    Paged<Note> result = noteRepository.search(authorId, query);
    // then
    assertEquals(1, result.data().size());
    assertEquals(2, result.total());
  }

  @Test
  void search_shouldReturnSharedNotes_whenIsSharedTrue() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    CreateNote createShared = NoteTestUtils.createCreateNoteBuilder().authorId(authorId).build();
    CreateNote createPrivate = NoteTestUtils.createCreateNoteBuilder().authorId(authorId).build();
    Note shared = new NoteImpl(createShared, noteValidator);
    shared.setPermissions(authorId, targetUserId, Set.of(NotePermission.READ));
    Note privateNote = new NoteImpl(createPrivate, noteValidator);
    NoteRepository noteRepository = createNoteRepository(shared, privateNote);
    NotePagedQuery query = createNotePagedQuery(true, null, 0, 10, null);
    // when
    Paged<Note> result = noteRepository.search(authorId, query);
    // then
    assertEquals(1, result.data().size());
    assertEquals(shared.getId(), result.data().getFirst().getId());
    assertEquals(1, result.total());
  }

  @Test
  void search_shouldReturnPrivateNotes_whenIsSharedFalse() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    CreateNote createShared = NoteTestUtils.createCreateNoteBuilder().authorId(authorId).build();
    CreateNote createPrivate = NoteTestUtils.createCreateNoteBuilder().authorId(authorId).build();
    Note shared = new NoteImpl(createShared, noteValidator);
    shared.setPermissions(authorId, targetUserId, Set.of(NotePermission.READ));
    Note privateNote = new NoteImpl(createPrivate, noteValidator);
    NoteRepository noteRepository = createNoteRepository(shared, privateNote);
    NotePagedQuery query = createNotePagedQuery(false, null, 0, 10, null);
    // when
    Paged<Note> result = noteRepository.search(authorId, query);
    // then
    assertEquals(1, result.data().size());
    assertEquals(privateNote.getId(), result.data().getFirst().getId());
    assertEquals(1, result.total());
  }

  @Test
  void search_shouldReturnBothOwnedAndSharedNotes_whenIsSharedNull() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    CreateNote createOwned = NoteTestUtils.createCreateNoteBuilder().authorId(authorId).build();
    CreateNote createShared =
        NoteTestUtils.createCreateNoteBuilder().authorId(UUID.randomUUID()).build();
    Note owned = new NoteImpl(createOwned, noteValidator);
    Note shared = new NoteImpl(createShared, noteValidator);
    shared.setPermissions(shared.getAuthorId(), authorId, Set.of(NotePermission.READ));
    NoteRepository noteRepository = createNoteRepository(owned, shared);
    NotePagedQuery query = createNotePagedQuery(null, null, 0, 10, null);
    // when
    Paged<Note> result = noteRepository.search(authorId, query);
    // then
    assertEquals(2, result.data().size());
    assertEquals(2, result.total());
    assertTrue(result.data().stream().anyMatch(n -> n.getId().equals(owned.getId())));
    assertTrue(result.data().stream().anyMatch(n -> n.getId().equals(shared.getId())));
  }

  @Test
  void deleteAll_shouldRemoveNotesNotes() {
    // given
    Note note1 = createNote();
    Note note2 = createNote();
    NoteRepository noteRepository = createNoteRepository(note1, note2);
    assertEquals(2, noteRepository.findNotes().size());
    // when
    noteRepository.deleteNotes();
    // then
    assertTrue(noteRepository.findNotes().isEmpty());
  }

  @Test
  void search_shouldReturnOnlySharedNotes_whenUserHasNoOwnedNotes() {
    // given
    UUID ownerId = UUID.randomUUID();
    UUID sharerId = UUID.randomUUID();
    UUID accessingUserId = UUID.randomUUID();
    CreateNote createShared = NoteTestUtils.createCreateNoteBuilder().authorId(sharerId).build();
    Note shared = new NoteImpl(createShared, noteValidator);
    shared.setPermissions(sharerId, accessingUserId, Set.of(NotePermission.READ));
    NoteRepository noteRepository = createNoteRepository(shared);
    NotePagedQuery query = createNotePagedQuery(null, null, 0, 10, null);
    // when
    Paged<Note> result = noteRepository.search(accessingUserId, query);
    // then
    assertEquals(1, result.data().size());
    assertEquals(shared.getId(), result.data().getFirst().getId());
    assertEquals(1, result.total());
  }

  @Test
  void search_shouldReturnOwnedNotes_whenUserIsOwner() {
    // given
    UUID authorId = UUID.randomUUID();
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().authorId(authorId).build();
    Note note = new NoteImpl(createNote, noteValidator);
    NoteRepository noteRepository = createNoteRepository(note);
    NotePagedQuery query = createNotePagedQuery(null, null, 0, 10, null);
    // when
    Paged<Note> result = noteRepository.search(authorId, query);
    // then
    assertEquals(1, result.data().size());
    assertEquals(note.getId(), result.data().getFirst().getId());
    assertEquals(1, result.total());
  }

  @Test
  void search_shouldReturnSharedNotes_whenUserHasSharedAccess() {
    // given
    UUID sharerId = UUID.randomUUID();
    UUID accessingUserId = UUID.randomUUID();
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().authorId(sharerId).build();
    Note note = new NoteImpl(createNote, noteValidator);
    note.setPermissions(sharerId, accessingUserId, Set.of(NotePermission.READ));
    NoteRepository noteRepository = createNoteRepository(note);
    NotePagedQuery query = createNotePagedQuery(null, null, 0, 10, null);
    // when
    Paged<Note> result = noteRepository.search(accessingUserId, query);
    // then
    assertEquals(1, result.data().size());
    assertEquals(note.getId(), result.data().getFirst().getId());
    assertEquals(1, result.total());
  }

  @Test
  void search_shouldNotReturnNotes_whenUserHasNoAccess() {
    // given
    UUID sharerId = UUID.randomUUID();
    UUID accessingUserId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().authorId(sharerId).build();
    Note note = new NoteImpl(createNote, noteValidator);
    note.setPermissions(sharerId, accessingUserId, Set.of(NotePermission.READ));
    NoteRepository noteRepository = createNoteRepository(note);
    NotePagedQuery query = createNotePagedQuery(null, null, 0, 10, null);
    // when
    Paged<Note> result = noteRepository.search(otherUserId, query);
    // then
    assertTrue(result.data().isEmpty());
    assertEquals(0, result.total());
  }

  @Test
  void search_shouldFilterBySearchQueryInTitle() {
    // given
    UUID authorId = UUID.randomUUID();
    CreateNote createNote1 =
        NoteTestUtils.createCreateNoteBuilder()
            .authorId(authorId)
            .title("Java Programming")
            .build();
    CreateNote createNote2 =
        NoteTestUtils.createCreateNoteBuilder().authorId(authorId).title("Python Guide").build();
    Note note1 = new NoteImpl(createNote1, noteValidator);
    Note note2 = new NoteImpl(createNote2, noteValidator);
    NoteRepository noteRepository = createNoteRepository(note1, note2);
    NotePagedQuery query = createNotePagedQuery(null, null, 0, 10, null, "Java");
    // when
    Paged<Note> result = noteRepository.search(authorId, query);
    // then
    assertEquals(1, result.data().size());
    assertEquals(note1.getId(), result.data().getFirst().getId());
    assertEquals(1, result.total());
  }

  @Test
  void search_shouldFilterBySearchQueryInContent() {
    // given
    UUID authorId = UUID.randomUUID();
    CreateNote createNote1 =
        NoteTestUtils.createCreateNoteBuilder()
            .authorId(authorId)
            .title("Guide")
            .content("Learn Spring Framework basics")
            .build();
    CreateNote createNote2 =
        NoteTestUtils.createCreateNoteBuilder()
            .authorId(authorId)
            .title("Guide")
            .content("Learn Python basics")
            .build();
    Note note1 = new NoteImpl(createNote1, noteValidator);
    Note note2 = new NoteImpl(createNote2, noteValidator);
    NoteRepository noteRepository = createNoteRepository(note1, note2);
    NotePagedQuery query = createNotePagedQuery(null, null, 0, 10, null, "Spring");
    // when
    Paged<Note> result = noteRepository.search(authorId, query);
    // then
    assertEquals(1, result.data().size());
    assertEquals(note1.getId(), result.data().getFirst().getId());
    assertEquals(1, result.total());
  }

  @Test
  void search_shouldFilterBySearchQueryCaseInsensitive() {
    // given
    UUID authorId = UUID.randomUUID();
    CreateNote createNote1 =
        NoteTestUtils.createCreateNoteBuilder()
            .authorId(authorId)
            .title("JAVA Programming")
            .build();
    Note note1 = new NoteImpl(createNote1, noteValidator);
    NoteRepository noteRepository = createNoteRepository(note1);
    NotePagedQuery query = createNotePagedQuery(null, null, 0, 10, null, "java");
    // when
    Paged<Note> result = noteRepository.search(authorId, query);
    // then
    assertEquals(1, result.data().size());
    assertEquals(note1.getId(), result.data().getFirst().getId());
  }

  @Test
  void search_shouldReturnNoNotes_whenSearchQueryDoesNotMatch() {
    // given
    UUID authorId = UUID.randomUUID();
    CreateNote createNote1 =
        NoteTestUtils.createCreateNoteBuilder().authorId(authorId).title("Java Guide").build();
    Note note1 = new NoteImpl(createNote1, noteValidator);
    NoteRepository noteRepository = createNoteRepository(note1);
    NotePagedQuery query = createNotePagedQuery(null, null, 0, 10, null, "Python");
    // when
    Paged<Note> result = noteRepository.search(authorId, query);
    // then
    assertTrue(result.data().isEmpty());
    assertEquals(0, result.total());
  }

  @Test
  void search_shouldReturnAllNotes_whenSearchQueryIsNull() {
    // given
    UUID authorId = UUID.randomUUID();
    CreateNote createNote1 =
        NoteTestUtils.createCreateNoteBuilder().authorId(authorId).title("Java Guide").build();
    CreateNote createNote2 =
        NoteTestUtils.createCreateNoteBuilder().authorId(authorId).title("Python Guide").build();
    Note note1 = new NoteImpl(createNote1, noteValidator);
    Note note2 = new NoteImpl(createNote2, noteValidator);
    NoteRepository noteRepository = createNoteRepository(note1, note2);
    NotePagedQuery query = createNotePagedQuery(null, null, 0, 10, null, null);
    // when
    Paged<Note> result = noteRepository.search(authorId, query);
    // then
    assertEquals(2, result.data().size());
    assertEquals(2, result.total());
  }

  @Test
  void search_shouldReturnAllNotes_whenSearchQueryIsEmpty() {
    // given
    UUID authorId = UUID.randomUUID();
    CreateNote createNote1 =
        NoteTestUtils.createCreateNoteBuilder().authorId(authorId).title("Java Guide").build();
    CreateNote createNote2 =
        NoteTestUtils.createCreateNoteBuilder().authorId(authorId).title("Python Guide").build();
    Note note1 = new NoteImpl(createNote1, noteValidator);
    Note note2 = new NoteImpl(createNote2, noteValidator);
    NoteRepository noteRepository = createNoteRepository(note1, note2);
    NotePagedQuery query = createNotePagedQuery(null, null, 0, 10, null, "  ");
    // when
    Paged<Note> result = noteRepository.search(authorId, query);
    // then
    assertEquals(2, result.data().size());
    assertEquals(2, result.total());
  }

  @Test
  void search_shouldCombineSearchQueryWithIsSharedFilter() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    CreateNote createShared =
        NoteTestUtils.createCreateNoteBuilder().authorId(authorId).title("Java Shared").build();
    CreateNote createPrivate =
        NoteTestUtils.createCreateNoteBuilder().authorId(authorId).title("Java Private").build();
    Note shared = new NoteImpl(createShared, noteValidator);
    shared.setPermissions(authorId, targetUserId, Set.of(NotePermission.READ));
    Note privateNote = new NoteImpl(createPrivate, noteValidator);
    NoteRepository noteRepository = createNoteRepository(shared, privateNote);
    NotePagedQuery query = createNotePagedQuery(true, null, 0, 10, null, "Java");
    // when
    Paged<Note> result = noteRepository.search(authorId, query);
    // then
    assertEquals(1, result.data().size());
    assertEquals(shared.getId(), result.data().getFirst().getId());
    assertEquals(1, result.total());
  }

  @Test
  void search_shouldCombineSearchQueryWithIsPinnedFilter() {
    // given
    UUID authorId = UUID.randomUUID();
    CreateNote createNote1 =
        NoteTestUtils.createCreateNoteBuilder().authorId(authorId).title("Java Guide").build();
    CreateNote createNote2 =
        NoteTestUtils.createCreateNoteBuilder().authorId(authorId).title("Java Tips").build();
    Note note1 = new NoteImpl(createNote1, noteValidator);
    note1.update(
        NoteTestUtils.createNoteUpdateBuilder()
            .actingUserId(authorId)
            .title("Java Guide")
            .pinned(true)
            .build());
    Note note2 = new NoteImpl(createNote2, noteValidator);
    NoteRepository noteRepository = createNoteRepository(note1, note2);
    NotePagedQuery query = createNotePagedQuery(null, true, 0, 10, null, "Java");
    // when
    Paged<Note> result = noteRepository.search(authorId, query);
    // then
    assertEquals(1, result.data().size());
    assertEquals(note1.getId(), result.data().getFirst().getId());
    assertEquals(1, result.total());
  }

  @Test
  void search_shouldCombineAllFilters() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    CreateNote createNote1 =
        NoteTestUtils.createCreateNoteBuilder().authorId(authorId).title("Java Shared").build();
    CreateNote createNote2 =
        NoteTestUtils.createCreateNoteBuilder().authorId(authorId).title("Python Shared").build();
    CreateNote createNote3 =
        NoteTestUtils.createCreateNoteBuilder().authorId(authorId).title("Java Private").build();
    Note note1 = new NoteImpl(createNote1, noteValidator);
    note1.setPermissions(authorId, targetUserId, Set.of(NotePermission.READ));
    note1.update(
        NoteTestUtils.createNoteUpdateBuilder()
            .actingUserId(authorId)
            .title("Java Shared")
            .pinned(true)
            .build());
    Note note2 = new NoteImpl(createNote2, noteValidator);
    note2.setPermissions(authorId, targetUserId, Set.of(NotePermission.READ));
    Note note3 = new NoteImpl(createNote3, noteValidator);
    NoteRepository noteRepository = createNoteRepository(note1, note2, note3);
    NotePagedQuery query = createNotePagedQuery(true, true, 0, 10, null, "Java");
    // when
    Paged<Note> result = noteRepository.search(authorId, query);
    // then
    assertEquals(1, result.data().size());
    assertEquals(note1.getId(), result.data().getFirst().getId());
    assertEquals(1, result.total());
  }

  private NoteRepository createNoteRepository(Note... notes) {
    return new InMemoryNoteRepository(Arrays.asList(notes));
  }

  private Note createNote() {
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    return new NoteImpl(createNote, noteValidator);
  }
}
