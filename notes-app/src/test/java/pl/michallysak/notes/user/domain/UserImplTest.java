package pl.michallysak.notes.user.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.user.UserTestUtils;
import pl.michallysak.notes.user.model.CreateUser;
import pl.michallysak.notes.user.validator.UserValidator;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserImplTest {
    
    @Mock
    private UserValidator userValidator;

    @Test
    void constructor_shouldThrowNullPointerException_whenNullUserValidator() {
        // given
        CreateUser createUser = UserTestUtils.createCreateUserBuilder().build();
        // when
        Executable executable = () -> new UserImpl(createUser, null);
        // then
        assertThrows(NullPointerException.class, executable);
    }

    @Test
    void constructor_shouldSetFieldsCorrectly_whenValidInput() {
        // given
        CreateUser createUser = UserTestUtils.createCreateUserBuilder().build();
        // when
        UserImpl user = new UserImpl(createUser, userValidator);
        // then
        assertNotNull(user.getId());
        assertEquals(createUser.email(), user.getEmail());
        assertEquals(createUser.password(), user.getPassword());
    }

}

