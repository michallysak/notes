package pl.michallysak.notes.note.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

  private final NoteRepository noteRepository;
  private final DomainEventPublisher eventPublisher;
  private final NoteValidator noteValidator;

  @Override
  public NoteValue createNote(CreateNote createNote) {
    Note note = new NoteImpl(createNote, noteValidator);
    noteRepository.saveNote(note);
    NoteValue noteValue = NoteValue.from(note);
    NoteCreatedEvent noteCreatedEvent = NoteCreatedEvent.from(noteValue);
    eventPublisher.publish(Collections.singletonList(noteCreatedEvent));
    return noteValue;
  }

  @Override
  public List<NoteValue> getCreatedNotes(UUID authorId) {
    return noteRepository.findNotesWithAuthor(authorId).stream()
        .peek(note -> note.read(authorId))
        .map(NoteValue::from)
        .toList();
  }

  @Override
  public Paged<NoteValue> search(UUID authorId, NotePagedQuery query) {
    noteValidator.validateNoteQuery(query);
    List<NoteValue> data =
        noteRepository.search(authorId, query).stream().map(NoteValue::from).toList();
    return new Paged<>(data, query.getPage(), query.getSize());
  }

  @Override
  public NoteValue getCreatedNote(UUID noteId, UUID authorId) throws NoteNotFoundException {
    Note note = noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
    note.read(authorId);
    return NoteValue.from(note);
  }

  @Override
  public NoteValue updateNote(UUID noteId, NoteUpdate noteUpdate) throws NoteNotFoundException {
    Note note = noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
    note.update(noteUpdate);
    noteRepository.saveNote(note);
    NoteValue noteValue = NoteValue.from(note);
    NoteUpdatedEvent noteUpdatedEvent = NoteUpdatedEvent.from(noteValue);
    eventPublisher.publish(Collections.singletonList(noteUpdatedEvent));
    return noteValue;
  }

  @Override
  public void deleteNote(UUID noteId, UUID actingUserId) throws NoteNotFoundException {
    Note note = noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
    note.delete(actingUserId);
    noteRepository.deleteNoteWithId(noteId);
    NoteValue noteValue = NoteValue.from(note);
    NoteDeletedEvent noteDeletedEvent = NoteDeletedEvent.from(noteValue);
    eventPublisher.publish(Collections.singletonList(noteDeletedEvent));
  }

  @Override
  public void setPermissions(UUID noteId, UUID actingUserId, SetNotePermissions request)
      throws NoteNotFoundException {
    Note note = noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
    note.setPermissions(actingUserId, request.targetUserId(), request.permissions());
    noteRepository.saveNote(note);
  }

  @Override
  public void removeAccess(UUID noteId, UUID actingUserId, UUID targetUserId)
      throws NoteNotFoundException {
    Note note = noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
    note.removeAccess(actingUserId, targetUserId);
    noteRepository.saveNote(note);
  }
}
