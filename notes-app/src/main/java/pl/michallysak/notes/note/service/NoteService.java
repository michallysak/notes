package pl.michallysak.notes.note.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import pl.michallysak.notes.note.exception.NoteNotFoundException;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NotePagedQuery;
import pl.michallysak.notes.note.model.NoteShare;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.model.Paged;
import pl.michallysak.notes.note.model.SetNotePermissions;

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
}
