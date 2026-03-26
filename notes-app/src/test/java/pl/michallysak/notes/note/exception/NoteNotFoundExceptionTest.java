package pl.michallysak.notes.note.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NoteNotFoundExceptionTest {

  @Test
  void constructor_shouldSetMessage() {
    // when
    NoteNotFoundException exception = new NoteNotFoundException();
    // then
    assertEquals("Note not found", exception.getMessage());
  }
}
