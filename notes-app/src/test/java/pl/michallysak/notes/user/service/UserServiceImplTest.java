package pl.michallysak.notes.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.user.UserTestUtils;
import pl.michallysak.notes.user.domain.User;
import pl.michallysak.notes.user.domain.UserImpl;
import pl.michallysak.notes.user.exception.UserNotFoundException;
import pl.michallysak.notes.user.model.CreateUser;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.repository.UserRepository;
import pl.michallysak.notes.user.validator.UserValidator;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserValidator userValidator;
    @InjectMocks
    private UserServiceImpl service;

    @Test
    void createUser_shouldSaveAndReturnValue() {
        // given
        CreateUser createUser = UserTestUtils.createCreateUserBuilder().build();
        // when
        UserValue userValue = service.createUser(createUser);
        // then
        verify(userRepository).save(any());
        assertNotNull(userValue.id());
        assertEquals(createUser.email(), userValue.email());
    }

    @Test
    void getUser_shouldReturnMappedValue() {
        // given
        User user = new UserImpl(UserTestUtils.createCreateUserBuilder().build(), userValidator);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        // when
        UserValue value = service.getUser(user.getId());
        // then
        assertEquals(user.getId(), value.id());
        assertEquals(user.getEmail(), value.email());
    }

    @Test
    void getUser_shouldThrow_whenNotFound() {
        // given
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        // when
        Executable executable = () -> service.getUser(id);
        // then
        assertThrows(UserNotFoundException.class, executable);
    }
}
