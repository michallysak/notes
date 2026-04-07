package pl.michallysak.notes.note.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.UUID;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;
import pl.michallysak.notes.note.domain.event.DomainEventPublisher;
import pl.michallysak.notes.note.domain.event.NoteCreatedEvent;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.repository.NoteRepository;

@ApplicationScoped
public class NoteApplicationService {
  @Inject NoteRepository repo;
  @Inject DomainEventPublisher publisher;

  public NoteValue createNote(CreateNote createNote) {
    Note note = new NoteImpl(createNote);
    repo.save(note);
    publisher.publish(List.of(new NoteCreatedEvent(note.getId(), note.getContent())));
    return NoteValue.from(note);
  }

  public NoteValue updateNote(UUID id, NoteUpdate noteUpdate) {
    Note note = repo.findById(id).orElseThrow();
    note.update(noteUpdate);
    repo.save(note);
    return NoteValue.from(note);
  }

  public void deleteNote(UUID id) {
    repo.deleteById(id);
  }
}
