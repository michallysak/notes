package pl.michallysak.notes.note.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pl.michallysak.notes.note.domain.Note;

public interface NoteRepository {

  void saveNote(Note note);

  List<Note> findNotes();

  List<Note> findNotesWithAuthor(UUID authorId);

  Optional<Note> findNoteWithId(UUID id);

  boolean deleteNoteWithId(UUID id);

  void deleteNotes();
}
