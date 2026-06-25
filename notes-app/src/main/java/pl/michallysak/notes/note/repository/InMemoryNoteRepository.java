package pl.michallysak.notes.note.repository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import pl.michallysak.notes.common.SortComparatorFactory;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.model.NotePagedQuery;
import pl.michallysak.notes.note.model.Paged;

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
  public Paged<Note> search(UUID actingUserId, NotePagedQuery query) {
    Comparator<Note> comparator = noteComparator.createComparator(query.getSort());
    List<Note> filtered =
        notes.values().stream()
            .filter(note -> isOwnedByOrSharedWith(actingUserId, note))
            .filter(
                note -> {
                  if (query.getIsShared() == null) {
                    return true;
                  }
                  boolean isShared = !note.getShares(actingUserId).isEmpty();
                  return query.getIsShared().equals(isShared);
                })
            .filter(
                note -> {
                  if (query.getIsPinned() == null) {
                    return true;
                  }
                  return query.getIsPinned().equals(note.isPinned());
                })
            .filter(
                note -> {
                  if (query.getSearchQuery() == null || query.getSearchQuery().isBlank()) {
                    return true;
                  }
                  String searchTerm = query.getSearchQuery().toLowerCase();
                  return note.getTitle().toLowerCase().contains(searchTerm)
                      || note.getContent().toLowerCase().contains(searchTerm);
                })
            .sorted(comparator)
            .toList();

    long total = filtered.size();
    List<Note> paged =
        filtered.stream()
            .skip((long) query.getPage() * query.getSize())
            .limit(query.getSize())
            .toList();

    return new Paged<>(paged, query.getPage(), query.getSize(), total);
  }

  private boolean isOwnedByOrSharedWith(UUID actingUserId, Note note) {
    return note.getAuthorId().equals(actingUserId)
        || note.getShares(actingUserId).stream()
            .anyMatch(share -> share.userId().equals(actingUserId));
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

  @Override
  public Optional<Note> findNoteByPublicShareId(UUID publicShareId) {
    return notes.values().stream()
        .filter(note -> note.getPublicShare().isPresent())
        .filter(note -> note.getPublicShare().get().publicShareId().equals(publicShareId))
        .findFirst();
  }
}
