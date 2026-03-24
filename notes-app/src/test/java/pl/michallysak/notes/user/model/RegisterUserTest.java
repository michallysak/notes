package pl.michallysak.notes.user.model;

import org.junit.jupiter.api.Test;
import pl.michallysak.notes.common.Email;
import static org.junit.jupiter.api.Assertions.*;

class RegisterUserTest {
    @Test
    void constructor_and_getters_shouldWork() {
        // given
        Email email = Email.of("user@example.com");
        String password = "Test123!@#";
        // when
        RegisterUser reg = new RegisterUser(email, password);
        // then
        assertEquals(email, reg.email());
        assertEquals(password, reg.password());
    }
    @Test
    void equals_and_hashCode_shouldWork() {
        // given
        Email email = Email.of("user@example.com");
        String password = "Test123!@#";
        // when
        RegisterUser registerUser1 = new RegisterUser(email, password);
        RegisterUser registerUser2 = new RegisterUser(email, password);
        // then
        assertEquals(registerUser1, registerUser2);
        assertEquals(registerUser1.hashCode(), registerUser2.hashCode());
    }
}
