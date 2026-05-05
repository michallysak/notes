package pl.michallysak.notes.application.quarkus.common;

import java.util.List;
import pl.michallysak.notes.note.model.FieldSort;

public record SortList(List<FieldSort> values) {
  public static SortList empty() {
    return new SortList(List.of());
  }
}
