package pl.michallysak.notes.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import pl.michallysak.notes.common.exception.ValidationException;

class EmailTest {
  @ParameterizedTest
  @ValueSource(
      strings = {
        "user@example.com",
        "user+test@example.com",
        "user-test@example.com",
        "user.test@example.com",
        "user@sub.example.com",
        "user123@example.co.uk"
      })
  void of_shouldCreateEmail_whenValid(String valid) {
    // when
    Email email = Email.of(valid);
    // then
    assertEquals(valid, email.getValue(), "Email should be valid: " + valid);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(
      strings = {
        "not-an-email",
        "user@.com",
        "@example.com",
        "user@example",
        "user@.example.com",
        "user@com",
        "user@-example.com",
        "user@example..com",
        "user@."
      })
  void of_shouldThrow_whenNullOrInvalidFormat(String invalid) {
    // when
    Executable executable = () -> Email.of(invalid);
    // then
    assertThrows(ValidationException.class, executable, "Email should be invalid: " + invalid);
  }
}
