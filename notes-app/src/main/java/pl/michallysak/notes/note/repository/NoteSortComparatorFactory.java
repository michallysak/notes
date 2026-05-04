package pl.michallysak.notes.note.repository;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import pl.michallysak.notes.common.SortComparatorFactory;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.model.FieldSort;

public class NoteSortComparatorFactory implements SortComparatorFactory<Note> {

  @Override
  public Comparator<Note> createComparator(List<FieldSort> fieldSorts) {
    return fieldSorts == null || fieldSorts.isEmpty()
        ? Comparator.comparing(Note::getCreated).reversed()
        : SortComparatorFactory.super.createComparator(fieldSorts);
  }

  @Override
  public Comparator<Note> createComparator(String field) {
    if (field == null) {
      return Comparator.comparing(Note::getCreated);
    }
    return switch (field) {
      case "updated" -> Comparator.comparing(note -> note.getUpdated().orElse(OffsetDateTime.MIN));
      case "title" -> Comparator.comparing(Note::getTitle);
      case "pinned" -> Comparator.comparing(Note::isPinned);
      default -> Comparator.comparing(Note::getCreated);
    };
  }
}
