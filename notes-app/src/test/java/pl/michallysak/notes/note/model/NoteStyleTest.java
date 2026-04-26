package pl.michallysak.notes.note.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class NoteStyleTest {

  @Test
  void constructor_shouldInitializeFieldsCorrectly() {
    // given
    String color = "#FF5733";
    // when
    NoteStyle noteStyle = NoteStyle.builder().color(color).build();
    // then
    assertNotNull(noteStyle);
    assertEquals(color, noteStyle.color());
  }
}
