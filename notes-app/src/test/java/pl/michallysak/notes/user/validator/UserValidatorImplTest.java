package pl.michallysak.notes.user.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pl.michallysak.notes.auth.validator.PasswordStrengthValidatorImpl;
import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.user.UserTestUtils;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;

import static org.junit.jupiter.api.Assertions.*;

class UserValidatorImplTest {
    private final UserValidatorImpl userValidator = new UserValidatorImpl(new PasswordStrengthValidatorImpl());

    @Test
    void validateCreateUser_shouldThrow_whenNull() {
        // given
        EmailPasswordCreateUser emailPasswordCreateUser = null;
        // when
        Executable executable = () -> userValidator.validateCreateUser(emailPasswordCreateUser);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        assertEquals("CreateUser cannot be null", validationException.getMessage());
    }

    @Test
    void validateCreateUser_shouldThrow_whenNullEmail() {
        // given
        EmailPasswordCreateUser emailPasswordCreateUser = UserTestUtils.createEmailPasswordCreateUserBuilder().email(null).build();
        // when
        Executable executable = () -> userValidator.validateCreateUser(emailPasswordCreateUser);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        assertEquals("Email cannot be null", validationException.getMessage());
    }

    @ParameterizedTest
    @MethodSource("pl.michallysak.notes.user.validator.UserValidatorImplTestUtils#createUsersWithNotInRangeLengthEmail")
    void validateCreateUser_shouldThrow_whenNotInRangeLengthEmail(EmailPasswordCreateUser emailPasswordCreateUser) {
        // when
        Executable executable = () -> userValidator.validateCreateUser(emailPasswordCreateUser);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        assertTrue(validationException.getMessage().contains("between"));
    }

    @Test
    void validateCreateUser_shouldPass_whenValidEmail() {
        // given
        EmailPasswordCreateUser emailPasswordCreateUser = UserTestUtils.createEmailPasswordCreateUserBuilder().build();
        // when
        Executable executable = () -> userValidator.validateCreateUser(emailPasswordCreateUser);
        // then
        assertDoesNotThrow(executable);
    }
}
