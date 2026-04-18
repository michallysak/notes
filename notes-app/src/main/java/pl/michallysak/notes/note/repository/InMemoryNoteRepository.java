package pl.michallysak.notes.note.repository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import pl.michallysak.notes.note.domain.Note;

public class InMemoryNoteRepository implements NoteRepository {
  private final Map<UUID, Note> notes;

  public InMemoryNoteRepository() {
    notes = new HashMap<>();
  }

  public InMemoryNoteRepository(List<Note> initialNotes) {
    this.notes = initialNotes.stream().collect(Collectors.toMap(Note::getId, Function.identity()));
  }

  @Override
  public void saveNote(Note note) {
    notes.put(note.getId(), note);
  }

  @Override
  public List<Note> findNotes() {
    return notes.values().stream().toList();
  }

  @Override
  public List<Note> findNotesWithAuthor(UUID authorId) {
    return notes.values().stream().filter(note -> note.getAuthorId().equals(authorId)).toList();
  }

  @Override
  public Optional<Note> findNoteWithId(UUID id) {
    return Optional.ofNullable(notes.get(id));
  }

  @Override
  public boolean deleteNoteWithId(UUID id) {
    return notes.remove(id) != null;
  }

  @Override
  public void deleteNotes() {
    notes.clear();
  }
}
