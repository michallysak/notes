package pl.michallysak.notes.note.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pl.michallysak.notes.note.domain.Note;

public interface NoteRepository {

  void save(Note note);

  List<Note> findAll();

  List<Note> findAllWithAuthor(UUID authorId);

  Optional<Note> findById(UUID id);

  boolean deleteById(UUID id);

  void deleteAll();
}
