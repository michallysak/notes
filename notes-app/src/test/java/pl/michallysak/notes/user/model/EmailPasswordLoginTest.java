package pl.michallysak.notes.user.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pl.michallysak.notes.auth.model.Password;
import pl.michallysak.notes.common.Email;

class EmailPasswordLoginTest {
  @Test
  void constructor_and_getters_shouldWork() {
    // given
    Email email = Email.of("user@example.com");
    Password password = Password.of("Test123!@#");
    // when
    EmailPasswordLogin login = new EmailPasswordLogin(email, password);
    // then
    assertEquals(email, login.email());
    assertEquals(password, login.password());
  }

  @Test
  void equals_and_hashCode_shouldWork() {
    // given
    Email email = Email.of("user@example.com");
    Password password = Password.of("Test123!@#");
    // when
    EmailPasswordLogin login1 = new EmailPasswordLogin(email, password);
    EmailPasswordLogin login2 = new EmailPasswordLogin(email, password);
    // then
    assertEquals(login1, login2);
    assertEquals(login1.hashCode(), login2.hashCode());
  }
}
