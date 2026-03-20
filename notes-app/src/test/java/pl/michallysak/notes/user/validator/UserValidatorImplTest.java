package pl.michallysak.notes.user.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.user.UserTestUtils;
import pl.michallysak.notes.user.model.CreateUser;

import static org.junit.jupiter.api.Assertions.*;

class UserValidatorImplTest {
    private final UserValidatorImpl userValidator = new UserValidatorImpl();

    @Test
    void validateCreateUser_shouldThrow_whenNull() {
        // given
        CreateUser createUser = null;
        // when
        Executable executable = () -> userValidator.validateCreateUser(createUser);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        assertEquals("CreateUser cannot be null", validationException.getMessage());
    }

    @Test
    void validateCreateUser_shouldThrow_whenNullEmail() {
        // given
        CreateUser createUser = UserTestUtils.createCreateUserBuilder().email(null).build();
        // when
        Executable executable = () -> userValidator.validateCreateUser(createUser);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        assertEquals("Email cannot be null", validationException.getMessage());
    }

    @ParameterizedTest
    @MethodSource("pl.michallysak.notes.user.validator.UserValidatorImplTestUtils#createUsersWithNotInRangeLengthEmail")
    void validateCreateUser_shouldThrow_whenNotInRangeLengthEmail(CreateUser createUser) {
        // when
        Executable executable = () -> userValidator.validateCreateUser(createUser);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        assertTrue(validationException.getMessage().contains("between"));
    }

    @Test
    void validateCreateUser_shouldPass_whenValidEmail() {
        // given
        CreateUser createUser = UserTestUtils.createCreateUserBuilder().build();
        // when
        Executable executable = () -> userValidator.validateCreateUser(createUser);
        // then
        assertDoesNotThrow(executable);
    }
}
