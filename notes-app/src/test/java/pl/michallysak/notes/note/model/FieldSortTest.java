package pl.michallysak.notes.note.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FieldSortTest {

  @Test
  void shouldCreateFieldSortWithFieldAndDirection() {
    // given
    String field = "title";
    SortDirection direction = SortDirection.ASC;
    // when
    FieldSort fieldSort = new FieldSort(field, direction);
    // then
    assertEquals(field, fieldSort.field());
    assertEquals(direction, fieldSort.direction());
  }
}
