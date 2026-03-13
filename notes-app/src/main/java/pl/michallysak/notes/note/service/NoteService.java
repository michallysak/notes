package pl.michallysak.notes.note.service;


import pl.michallysak.notes.note.exception.NoteNotFoundException;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;

import java.util.List;
import java.util.UUID;

public interface NoteService {

    NoteValue createNote(CreateNote createNote);

    List<NoteValue> getCreatedNotes();

    NoteValue getCreatedNote(UUID noteId) throws NoteNotFoundException;

    NoteValue updateNote(UUID id, NoteUpdate noteUpdate);

    void deleteNote(UUID noteId);

}
