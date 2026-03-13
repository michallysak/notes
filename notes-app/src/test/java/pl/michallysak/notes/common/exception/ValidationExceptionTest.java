package pl.michallysak.notes.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        // given
        String message = "Validation failed";
        // when
        ValidationException exception = new ValidationException(message);
        // then
        assertEquals(message, exception.getMessage());
    }

}

