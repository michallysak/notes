package pl.michallysak.notes.note.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SortDirectionTest {

  @Test
  void shouldHaveAscAndDescValues() {
    // when
    SortDirection[] values = SortDirection.values();
    // then
    assertEquals(2, values.length);
    assertArrayEquals(new SortDirection[] {SortDirection.ASC, SortDirection.DESC}, values);
  }
}
