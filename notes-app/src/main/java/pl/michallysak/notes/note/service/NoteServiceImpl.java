package pl.michallysak.notes.note.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;
import pl.michallysak.notes.note.domain.event.DomainEventPublisher;
import pl.michallysak.notes.note.domain.event.NoteAccessRemovedEvent;
import pl.michallysak.notes.note.domain.event.NoteCreatedEvent;
import pl.michallysak.notes.note.domain.event.NoteDeletedEvent;
import pl.michallysak.notes.note.domain.event.NotePermissionsSetEvent;
import pl.michallysak.notes.note.domain.event.NotePublicShareRemovedEvent;
import pl.michallysak.notes.note.domain.event.NotePublicShareUpsertedEvent;
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
    NoteValue noteValue = NoteValue.from(note, note.getAuthorId());
    NoteCreatedEvent noteCreatedEvent = NoteCreatedEvent.from(noteValue);
    eventPublisher.publish(Collections.singletonList(noteCreatedEvent));
    return noteValue;
  }

  @Override
  public List<NoteValue> getCreatedNotes(UUID authorId) {
    return noteRepository.findNotesWithAuthor(authorId).stream()
        .peek(note -> note.read(authorId))
        .map(NoteValue::fromAuthor)
        .toList();
  }

  @Override
  public Paged<NoteValue> search(UUID actingUserId, NotePagedQuery query) {
    noteValidator.validateNoteQuery(query);
    Paged<Note> pagedNotes = noteRepository.search(actingUserId, query);
    List<NoteValue> data =
        pagedNotes.data().stream().map(note -> NoteValue.from(note, actingUserId)).toList();
    return new Paged<>(data, pagedNotes.page(), pagedNotes.size(), pagedNotes.total());
  }

  @Override
  public NoteValue getCreatedNote(UUID noteId, UUID authorId) throws NoteNotFoundException {
    Note note = noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
    note.read(authorId);
    return NoteValue.fromAuthor(note);
  }

  @Override
  public NoteValue updateNote(UUID noteId, NoteUpdate noteUpdate) throws NoteNotFoundException {
    Note note = noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
    note.update(noteUpdate);
    noteRepository.saveNote(note);
    NoteValue noteValue = NoteValue.from(note, noteUpdate.actingUserId());
    NoteUpdatedEvent noteUpdatedEvent = NoteUpdatedEvent.from(noteValue);
    eventPublisher.publish(Collections.singletonList(noteUpdatedEvent));
    return noteValue;
  }

  @Override
  public void deleteNote(UUID noteId, UUID actingUserId) throws NoteNotFoundException {
    Note note = noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
    note.delete(actingUserId);
    noteRepository.deleteNoteWithId(noteId);
    NoteValue noteValue = NoteValue.from(note, actingUserId);
    NoteDeletedEvent noteDeletedEvent = NoteDeletedEvent.from(noteValue);
    eventPublisher.publish(Collections.singletonList(noteDeletedEvent));
  }

  @Override
  public void setPermissions(UUID noteId, UUID actingUserId, SetNotePermissions request)
      throws NoteNotFoundException {
    Note note = noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
    note.setPermissions(actingUserId, request.targetUserId(), request.permissions());
    noteRepository.saveNote(note);
    NotePermissionsSetEvent notePermissionsSetEvent =
        NotePermissionsSetEvent.from(
            noteId, actingUserId, request.targetUserId(), request.permissions());
    eventPublisher.publish(Collections.singletonList(notePermissionsSetEvent));
  }

  @Override
  public void removeAccess(UUID noteId, UUID actingUserId, UUID targetUserId)
      throws NoteNotFoundException {
    Note note = noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
    note.removeAccess(actingUserId, targetUserId);
    noteRepository.saveNote(note);
    NoteAccessRemovedEvent noteAccessRemovedEvent =
        NoteAccessRemovedEvent.from(noteId, actingUserId, targetUserId);
    eventPublisher.publish(Collections.singletonList(noteAccessRemovedEvent));
  }

  @Override
  public Set<NoteShare> getPermissions(UUID noteId, UUID actingUserId)
      throws NoteNotFoundException {
    Note note = noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
    note.read(actingUserId);
    return note.getShares(actingUserId);
  }

  @Override
  public Set<NoteShare> getEffectivePermissions(UUID noteId, UUID actingUserId)
      throws NoteNotFoundException {
    Note note = noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
    note.read(actingUserId);
    return note.getEffectivePermissions(actingUserId);
  }

  @Override
  public UUID makeNotePublic(UUID noteId, UUID actingUserId, Set<NotePermission> notePermission) {
    Note note = noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
    UUID publicShareId = note.makeNotePublic(actingUserId, notePermission);
    noteRepository.saveNote(note);
    NoteValue noteValue = NoteValue.from(note, actingUserId);
    NotePublicShareUpsertedEvent notePublicShareUpsertedEvent =
        NotePublicShareUpsertedEvent.from(noteValue);
    eventPublisher.publish(Collections.singletonList(notePublicShareUpsertedEvent));
    return publicShareId;
  }

  @Override
  public void undoNotePublic(UUID noteId, UUID actingUserId) {
    Note note = noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
    UUID publicShareId = note.getPublicShare().map(NotePublicShare::publicShareId).orElse(null);
    note.undoNotePublic(actingUserId);
    noteRepository.saveNote(note);
    if (publicShareId == null) {
      return;
    }
    NoteValue noteValue = NoteValue.from(note, actingUserId);
    NotePublicShareRemovedEvent notePublicShareRemovedEvent =
        NotePublicShareRemovedEvent.from(noteValue, publicShareId);
    eventPublisher.publish(Collections.singletonList(notePublicShareRemovedEvent));
  }

  @Override
  public NoteValue getPublicNote(UUID publicShareId, UUID actingUserId) {
    Note note =
        noteRepository
            .findNoteByPublicShareId(publicShareId)
            .orElseThrow(NoteNotFoundException::new);
    note.read(actingUserId);
    return NoteValue.from(note, actingUserId);
  }
}
