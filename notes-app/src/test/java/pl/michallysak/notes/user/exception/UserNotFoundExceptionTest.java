package pl.michallysak.notes.user.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserNotFoundExceptionTest {
    @Test
    void defaultMessage_shouldBeUserNotFound() {
        // when
        UserNotFoundException exception = new UserNotFoundException();
        // then
        assertEquals("User not found", exception.getMessage());
    }
}

