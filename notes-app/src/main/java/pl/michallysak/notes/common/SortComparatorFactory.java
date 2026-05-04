package pl.michallysak.notes.common;

import java.util.Comparator;
import java.util.List;
import pl.michallysak.notes.note.model.FieldSort;
import pl.michallysak.notes.note.model.SortDirection;

public interface SortComparatorFactory<T> {

  default Comparator<T> createComparator(List<FieldSort> fieldSorts) {
    Comparator<T> comparator = null;
    for (FieldSort fieldSort : fieldSorts) {
      Comparator<T> fieldComparator = createComparator(fieldSort.field());
      if (fieldSort.direction() == SortDirection.DESC) {
        fieldComparator = fieldComparator.reversed();
      }
      if (comparator == null) {
        comparator = fieldComparator;
      } else {
        comparator = comparator.thenComparing(fieldComparator);
      }
    }
    return comparator;
  }

  Comparator<T> createComparator(String field);
}
