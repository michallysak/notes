package pl.michallysak.notes.note.service;

import java.util.List;
import java.util.UUID;
import pl.michallysak.notes.note.exception.NoteNotFoundException;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;

public interface NoteService {

  NoteValue createNote(CreateNote createNote);

  List<NoteValue> getCreatedNotes(UUID authorId);

  NoteValue getCreatedNote(UUID noteId, UUID authorId) throws NoteNotFoundException;

  NoteValue updateNote(UUID noteId, NoteUpdate noteUpdate) throws NoteNotFoundException;

  void deleteNote(UUID noteId, UUID actingUserId) throws NoteNotFoundException;
}
