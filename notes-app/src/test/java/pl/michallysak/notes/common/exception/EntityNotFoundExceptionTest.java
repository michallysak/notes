package pl.michallysak.notes.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
