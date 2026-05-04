package pl.michallysak.notes.note.repository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import pl.michallysak.notes.common.SortComparatorFactory;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.model.NotePagedQuery;

public class InMemoryNoteRepository implements NoteRepository {
  private final Map<UUID, Note> notes;
  private final SortComparatorFactory<Note> noteComparator = new NoteSortComparatorFactory();

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
  public List<Note> search(UUID authorId, NotePagedQuery query) {
    Comparator<Note> comparator = noteComparator.createComparator(query.getSort());
    return notes.values().stream()
        .filter(note -> isOwnedByOrSharedWith(authorId, note))
        .filter(
            note -> {
              if (query.getIsShared() == null) {
                return true;
              }
              boolean isShared = !note.getShares().isEmpty();
              return query.getIsShared().equals(isShared);
            })
        .sorted(comparator)
        .skip(query.getPage() * query.getSize())
        .limit(query.getSize())
        .toList();
  }

  private boolean isOwnedByOrSharedWith(UUID authorId, Note note) {
    return note.getAuthorId().equals(authorId)
        || note.getShares().stream().anyMatch(share -> share.userId().equals(authorId));
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
