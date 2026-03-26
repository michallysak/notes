package pl.michallysak.notes.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EntityNotFoundExceptionTest {

  @Test
  void constructor_shouldSetMessage() {
    // given
    String message = "Entity not found";
    // when
    EntityNotFoundException exception = new EntityNotFoundException(message);
    // then
    assertEquals(message, exception.getMessage());
  }
}
