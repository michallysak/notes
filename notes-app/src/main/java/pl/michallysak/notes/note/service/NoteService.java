package pl.michallysak.notes.note.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import pl.michallysak.notes.note.exception.NoteNotFoundException;
import pl.michallysak.notes.note.model.*;

public interface NoteService {

  NoteValue createNote(CreateNote createNote);

  List<NoteValue> getCreatedNotes(UUID authorId);

  Paged<NoteValue> search(UUID actingUserId, NotePagedQuery query);

  NoteValue getCreatedNote(UUID noteId, UUID authorId) throws NoteNotFoundException;

  NoteValue updateNote(UUID noteId, NoteUpdate noteUpdate) throws NoteNotFoundException;

  void deleteNote(UUID noteId, UUID actingUserId) throws NoteNotFoundException;

  void setPermissions(UUID noteId, UUID actingUserId, SetNotePermissions request)
      throws NoteNotFoundException;

  void removeAccess(UUID noteId, UUID actingUserId, UUID targetUserId) throws NoteNotFoundException;

  Set<NoteShare> getPermissions(UUID noteId, UUID actingUserId) throws NoteNotFoundException;

  Set<NoteShare> getEffectivePermissions(UUID noteId, UUID actingUserId)
      throws NoteNotFoundException;

  UUID makeNotePublic(UUID noteId, UUID actingUserId, Set<NotePermission> permissions);

  void undoNotePublic(UUID noteId, UUID actingUserId);

  NoteValue getPublicNote(UUID publicShareId, UUID actingUserId);
}
