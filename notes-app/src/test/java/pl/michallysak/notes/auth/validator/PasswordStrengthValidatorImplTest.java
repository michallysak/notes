package pl.michallysak.notes.auth.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import pl.michallysak.notes.auth.model.Password;
import pl.michallysak.notes.common.exception.ValidationException;
import static org.junit.jupiter.api.Assertions.*;

class PasswordStrengthValidatorImplTest {

    private final PasswordStrengthValidatorImpl validator = new PasswordStrengthValidatorImpl();

    @Test
    void validatePassword_shouldAcceptStrongPassword() {
        // given
        Password password = Password.of("Test123!@#");
        // when
        Executable executable = () -> validator.validatePassword(password);
        // then
        assertDoesNotThrow(executable);
    }

    @Test
    void validatePassword_shouldRejectNull() {
        // given
        Password password = null;
        // when
        Executable executable = () -> validator.validatePassword(password);
        // then
        ValidationException exception = assertThrows(ValidationException.class, executable);
        assertEquals("Password cannot be null.", exception.getMessage());
    }

    @Test
    void validatePassword_shouldRejectShortPassword() {
        // given
        Password password = Password.of("aA1!");
        // when
        Executable executable = () -> validator.validatePassword(password);
        // then
        ValidationException exception = assertThrows(ValidationException.class, executable);
        assertTrue(exception.getMessage().contains("Password must be between"));
    }

    @Test
    void validatePassword_shouldRejectMissingUppercase() {
        // given
        Password password = Password.of("test123!@#");
        // when
        Executable executable = () -> validator.validatePassword(password);
        // then
        ValidationException exception = assertThrows(ValidationException.class, executable);
        assertTrue(exception.getMessage().contains("Password must be between"));
    }

    @Test
    void validatePassword_shouldRejectMissingDigit() {
        // given
        Password password = Password.of("Testtest!@#");
        // when
        Executable executable = () -> validator.validatePassword(password);
        // then
        ValidationException exception = assertThrows(ValidationException.class, executable);
        assertTrue(exception.getMessage().contains("Password must be between"));
    }

    @Test
    void validatePassword_shouldRejectMissingSpecial() {
        // given
        Password password = Password.of("Test12345");
        // when
        Executable executable = () -> validator.validatePassword(password);
        // then
        ValidationException exception = assertThrows(ValidationException.class, executable);
        assertTrue(exception.getMessage().contains("Password must be between"));
    }
}
