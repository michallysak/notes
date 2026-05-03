package pl.michallysak.notes.note.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.model.NotePagedQuery;

public interface NoteRepository {

  void saveNote(Note note);

  List<Note> findNotes();

  List<Note> findNotesWithAuthor(UUID authorId);

  List<Note> search(UUID authorId, NotePagedQuery query);

  Optional<Note> findNoteWithId(UUID id);

  boolean deleteNoteWithId(UUID id);

  void deleteNotes();
}
