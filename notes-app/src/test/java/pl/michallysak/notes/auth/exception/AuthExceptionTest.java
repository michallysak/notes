package pl.michallysak.notes.auth.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthExceptionTest {
    @Test
    void constructor_withMessage_shouldSetMessage() {
        // when
        AuthException exception = new AuthException("fail");
        // then
        assertEquals("fail", exception.getMessage());
    }
    @Test
    void constructor_withMessageAndCause_shouldSetBoth() {
        // given
        Exception cause = new Exception("cause");
        // when
        AuthException exception = new AuthException("fail", cause);
        // then
        assertEquals("fail", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
