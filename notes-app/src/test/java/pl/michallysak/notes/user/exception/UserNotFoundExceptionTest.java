package pl.michallysak.notes.user.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserNotFoundExceptionTest {
  @Test
  void defaultMessage_shouldBeUserNotFound() {
    // when
    UserNotFoundException exception = new UserNotFoundException();
    // then
    assertEquals("User not found", exception.getMessage());
  }
}
