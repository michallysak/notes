package pl.michallysak.notes.auth.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {
    @Test
    void of_shouldCreatePassword_whenValid() {
        // given
        String passwordValue = "Test123!@#";
        // when
        Password password = Password.of(passwordValue);
        // then
        assertEquals(passwordValue, password.getValue());
        assertFalse(password.toString().contains(passwordValue));
    }

    @Test
    void constructor_shouldThrow_whenNull() {
        // given
        String passwordValue = null;
        // when
        Executable executable = () -> Password.of(passwordValue);
        // then
        Exception exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Password cannot be null or blank", exception.getMessage());
    }

    @Test
    void constructor_shouldThrow_whenBlank() {
        // given
        String passwordValue = "   ";
        // when
        Executable executable = () -> Password.of(passwordValue);
        // then
        Exception exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Password cannot be null or blank", exception.getMessage());
    }

    @Test
    void equals_and_hashCode_shouldWork() {
        // given
        String passworrValue = "abc";
        // when
        Password password1 = Password.of(passworrValue);
        Password password2 = Password.of(passworrValue);
        // then
        assertEquals(password1, password2);
        assertEquals(password1.hashCode(), password2.hashCode());
    }
}
