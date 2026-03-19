package pl.michallysak.notes.note.repository;


import pl.michallysak.notes.note.domain.Note;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NoteRepository {

    void save(Note note);

    List<Note> findAll();

    List<Note> findAllWithAuthor(UUID authorId);

    Optional<Note> findById(UUID id);

    boolean deleteById(UUID id);

}
