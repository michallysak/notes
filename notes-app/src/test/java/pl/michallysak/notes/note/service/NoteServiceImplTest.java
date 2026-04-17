package pl.michallysak.notes.note.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
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
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.repository.NoteRepository;
import pl.michallysak.notes.user.service.NoAuthCurrentUserProvider;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

  @Mock private NoteRepository repository;

  @Mock private DomainEventPublisher eventPublisher = events -> {};

  @InjectMocks private NoteServiceImpl service;

  private static final UUID AUTHOR_ID = new NoAuthCurrentUserProvider().getCurrentUserId();

  @Test
  void createNote_shouldValidateSaveLogAndPublishEvent() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    // when
    NoteValue noteValue = service.createNote(createNote);
    // then
    verify(repository).save(any());
    verify(eventPublisher)
        .publish(argThat(events -> events.stream().anyMatch(e -> e instanceof NoteCreatedEvent)));
    assertEquals(createNote.title(), noteValue.title());
    assertEquals(createNote.content(), noteValue.content());
  }

  @Test
  void getCreatedNotes_shouldReturnMappedValues() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote);
    when(repository.findAllWithAuthor(eq(AUTHOR_ID))).thenReturn(List.of(note));
    // when
    List<NoteValue> noteValues = service.getCreatedNotes(AUTHOR_ID);
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
    NoteValue noteValue = service.getCreatedNote(id, AUTHOR_ID);
    // then
    assertEquals(NoteValue.from(note), noteValue);
  }

  @Test
  void getCreatedNote_shouldThrow_whenNotExists() {
    // given
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    // when
    Executable executable = () -> service.getCreatedNote(id, AUTHOR_ID);
    // then
    assertThrows(NoteNotFoundException.class, executable);
  }

  @Test
  void updateNote_shouldValidateAndSave() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote);
    UUID id = note.getId();
    NoteUpdate update =
        NoteTestUtils.createNoteUpdateBuilder()
            .actingUserId(createNote.authorId())
            .pinned(null)
            .build();
    when(repository.findById(id)).thenReturn(Optional.of(note));
    // when
    NoteValue noteValue = service.updateNote(id, update);
    // then
    verify(repository).save(note);
    verify(eventPublisher)
        .publish(argThat(events -> events.stream().anyMatch(e -> e instanceof NoteUpdatedEvent)));
    assertEquals(NoteValue.from(note), noteValue);
  }

  @Test
  void deleteNote_shouldDelete() {
    // given
    UUID id = UUID.randomUUID();
    Note note = new NoteImpl(NoteTestUtils.createCreateNoteBuilder().build());
    when(repository.findById(id)).thenReturn(Optional.of(note));
    when(repository.deleteById(id)).thenReturn(true);
    // when
    service.deleteNote(id, AUTHOR_ID);
    // then
    verify(repository).deleteById(id);
    verify(eventPublisher)
        .publish(argThat(events -> events.stream().anyMatch(e -> e instanceof NoteDeletedEvent)));
  }

  @Test
  void deleteNote_shouldThrow_whenNotExists() {
    // given
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    // when
    Executable executable = () -> service.deleteNote(id, AUTHOR_ID);
    // then
    assertThrows(NoteNotFoundException.class, executable);
  }
}
