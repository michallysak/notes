package pl.michallysak.notes.note.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;
import pl.michallysak.notes.note.domain.event.DomainEventPublisher;
import pl.michallysak.notes.note.domain.event.NoteCreatedEvent;
import pl.michallysak.notes.note.exception.NoteNotFoundException;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.repository.NoteRepository;

@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

  private final NoteRepository noteRepository;
  private final DomainEventPublisher eventPublisher;

  @Override
  public NoteValue createNote(CreateNote createNote) {
    Note note = new NoteImpl(createNote);
    noteRepository.save(note);
    eventPublisher.publish(List.of(new NoteCreatedEvent(note.getId(), note.getContent())));
    return NoteValue.from(note);
  }

  @Override
  public List<NoteValue> getCreatedNotes(UUID authorId) {
    return noteRepository.findAllWithAuthor(authorId).stream()
        .peek(note -> note.read(authorId))
        .map(NoteValue::from)
        .toList();
  }

  @Override
  public NoteValue getCreatedNote(UUID noteId, UUID authorId) throws NoteNotFoundException {
    Note note = noteRepository.findById(noteId).orElseThrow(NoteNotFoundException::new);
    note.read(authorId);
    return NoteValue.from(note);
  }

  @Override
  public NoteValue updateNote(UUID noteId, NoteUpdate noteUpdate) throws NoteNotFoundException {
    Note note = noteRepository.findById(noteId).orElseThrow(NoteNotFoundException::new);
    note.update(noteUpdate);
    noteRepository.save(note);
    return NoteValue.from(note);
  }

  @Override
  public void deleteNote(UUID noteId, UUID actingUserId) throws NoteNotFoundException {
    Note note = noteRepository.findById(noteId).orElseThrow(NoteNotFoundException::new);
    note.delete(actingUserId);
    noteRepository.deleteById(noteId);
  }
}
