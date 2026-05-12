package pl.michallysak.notes.note.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static pl.michallysak.notes.note.NoteTestUtils.createNotePagedQuery;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;
import pl.michallysak.notes.note.domain.event.DomainEventPublisher;
import pl.michallysak.notes.note.domain.event.NoteCreatedEvent;
import pl.michallysak.notes.note.domain.event.NoteDeletedEvent;
import pl.michallysak.notes.note.domain.event.NoteUpdatedEvent;
import pl.michallysak.notes.note.exception.NoteNotFoundException;
import pl.michallysak.notes.note.model.*;
import pl.michallysak.notes.note.repository.NoteRepository;
import pl.michallysak.notes.note.validator.NoteValidator;
import pl.michallysak.notes.user.service.NoAuthCurrentUserProvider;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

  @Mock private NoteRepository repository;

  @Mock private DomainEventPublisher eventPublisher = events -> {};

  @Mock private NoteValidator noteValidator;

  @InjectMocks private NoteServiceImpl service;

  private static final UUID AUTHOR_ID = new NoAuthCurrentUserProvider().getCurrentUserId();

  @Test
  void createNote_shouldValidateSaveLogAndPublishEvent() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    // when
    NoteValue noteValue = service.createNote(createNote);
    // then
    verify(repository).saveNote(any());
    verify(eventPublisher)
        .publish(argThat(events -> events.stream().anyMatch(e -> e instanceof NoteCreatedEvent)));
    assertEquals(createNote.title(), noteValue.title());
    assertEquals(createNote.content(), noteValue.content());
  }

  @Test
  void getCreatedNotes_shouldReturnMappedValues() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    when(repository.findNotesWithAuthor(eq(AUTHOR_ID))).thenReturn(List.of(note));
    // when
    List<NoteValue> noteValues = service.getCreatedNotes(AUTHOR_ID);
    // then
    assertEquals(1, noteValues.size());
    assertEquals(NoteValue.fromAuthor(note), noteValues.getFirst());
  }

  @Test
  void search_shouldValidateDelegateAndMapValues() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().authorId(AUTHOR_ID).build();
    Note note = new NoteImpl(createNote, noteValidator);
    NotePagedQuery query = createNotePagedQuery(null, null, 0, 20, null);
    when(repository.search(AUTHOR_ID, query)).thenReturn(new Paged<>(List.of(note), 0, 20, 1));
    // when
    Paged<NoteValue> response = service.search(AUTHOR_ID, query);
    // then
    verify(noteValidator).validateNoteQuery(query);
    verify(repository).search(AUTHOR_ID, query);
    assertEquals(1, response.data().size());
    assertEquals(NoteValue.fromAuthor(note), response.data().getFirst());
    assertEquals(0, response.page());
    assertEquals(20, response.size());
    assertEquals(1, response.total());
  }

  @Test
  void getCreatedNote_shouldReturnMappedValue() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID id = note.getId();
    when(repository.findNoteWithId(id)).thenReturn(Optional.of(note));
    // when
    NoteValue noteValue = service.getCreatedNote(id, AUTHOR_ID);
    // then
    assertEquals(NoteValue.fromAuthor(note), noteValue);
  }

  @Test
  void getCreatedNote_shouldThrow_whenNotExists() {
    // given
    UUID id = UUID.randomUUID();
    when(repository.findNoteWithId(id)).thenReturn(Optional.empty());
    // when
    Executable executable = () -> service.getCreatedNote(id, AUTHOR_ID);
    // then
    assertThrows(NoteNotFoundException.class, executable);
  }

  @Test
  void updateNote_shouldValidateAndSave() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID id = note.getId();
    NoteUpdate update =
        NoteTestUtils.createNoteUpdateBuilder()
            .actingUserId(createNote.authorId())
            .pinned(null)
            .build();
    when(repository.findNoteWithId(id)).thenReturn(Optional.of(note));
    // when
    NoteValue noteValue = service.updateNote(id, update);
    // then
    verify(repository).saveNote(note);
    verify(eventPublisher)
        .publish(argThat(events -> events.stream().anyMatch(e -> e instanceof NoteUpdatedEvent)));
    assertEquals(NoteValue.fromAuthor(note), noteValue);
  }

  @Test
  void deleteNote_shouldDelete() {
    // given
    UUID id = UUID.randomUUID();
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    when(repository.findNoteWithId(id)).thenReturn(Optional.of(note));
    when(repository.deleteNoteWithId(id)).thenReturn(true);
    // when
    service.deleteNote(id, AUTHOR_ID);
    // then
    verify(repository).deleteNoteWithId(id);
    verify(eventPublisher)
        .publish(argThat(events -> events.stream().anyMatch(e -> e instanceof NoteDeletedEvent)));
  }

  @Test
  void deleteNote_shouldThrow_whenNotExists() {
    // given
    UUID id = UUID.randomUUID();
    when(repository.findNoteWithId(id)).thenReturn(Optional.empty());
    // when
    Executable executable = () -> service.deleteNote(id, AUTHOR_ID);
    // then
    assertThrows(NoteNotFoundException.class, executable);
  }

  @Test
  void setPermissions_shouldSaveAndPublishUpdateEvent_whenNoteExists() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID noteId = note.getId();
    UUID targetUserId = UUID.randomUUID();
    Set<NotePermission> permissions = Set.of(NotePermission.READ);
    SetNotePermissions setNotePermissions = new SetNotePermissions(targetUserId, permissions);
    when(repository.findNoteWithId(noteId)).thenReturn(Optional.of(note));
    // when
    service.setPermissions(noteId, AUTHOR_ID, setNotePermissions);
    // then
    verify(repository).saveNote(note);
  }

  @Test
  void setPermissions_shouldThrow_whenNotExists() {
    // given
    UUID noteId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    SetNotePermissions request = new SetNotePermissions(targetUserId, Set.of(NotePermission.READ));
    when(repository.findNoteWithId(noteId)).thenReturn(Optional.empty());
    // when
    Executable executable = () -> service.setPermissions(noteId, AUTHOR_ID, request);
    // then
    assertThrows(NoteNotFoundException.class, executable);
  }

  @Test
  void removeAccess_shouldSaveAndPublishUpdateEvent_whenNoteExists() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID noteId = note.getId();
    UUID targetUserId = UUID.randomUUID();
    when(repository.findNoteWithId(noteId)).thenReturn(Optional.of(note));
    // when
    service.removeAccess(noteId, AUTHOR_ID, targetUserId);
    // then
    verify(repository).saveNote(note);
  }

  @Test
  void removeAccess_shouldThrow_whenNotExists() {
    // given
    UUID noteId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    when(repository.findNoteWithId(noteId)).thenReturn(Optional.empty());
    // when
    Executable executable = () -> service.removeAccess(noteId, AUTHOR_ID, targetUserId);
    // then
    assertThrows(NoteNotFoundException.class, executable);
  }

  @Test
  void getPermissions_shouldReturnVisibleShares() {
    // given
    UUID noteId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Note note = mock(Note.class);
    Set<NoteShare> expectedShares = Set.of(new NoteShare(userId, Set.of(NotePermission.READ)));
    when(repository.findNoteWithId(noteId)).thenReturn(Optional.of(note));
    when(note.getShares(userId)).thenReturn(expectedShares);
    // when
    Set<NoteShare> result = service.getPermissions(noteId, userId);
    // then
    assertEquals(expectedShares, result);
    verify(note).read(userId);
    verify(note).getShares(userId);
  }

  @Test
  void getPermissions_shouldThrow_whenNoteNotFound() {
    // given
    UUID noteId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(repository.findNoteWithId(noteId)).thenReturn(Optional.empty());
    // when
    Executable executable = () -> service.getPermissions(noteId, userId);
    // then
    assertThrows(NoteNotFoundException.class, executable);
  }
}
