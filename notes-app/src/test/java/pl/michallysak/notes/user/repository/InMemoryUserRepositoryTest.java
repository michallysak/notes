package pl.michallysak.notes.user.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.UserTestUtils;
import pl.michallysak.notes.user.domain.User;
import pl.michallysak.notes.user.domain.UserImpl;
import pl.michallysak.notes.user.validator.UserValidator;

@ExtendWith(MockitoExtension.class)
class InMemoryUserRepositoryTest {

  @Mock private UserValidator userValidator;

  @Test
  void findUserWithId_shouldReturnEmpty_whenNoExists() {
    // given
    UserRepository userRepository = new InMemoryUserRepository();
    UUID randomId = UUID.randomUUID();
    // when
    Optional<User> userOptional = userRepository.findUserWithId(randomId);
    // then
    assertTrue(userOptional.isEmpty());
  }

  @Test
  void findUserWithId_shouldReturnIt_whenExists() {
    // given
    User user =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    UserRepository userRepository = new InMemoryUserRepository(Collections.singletonList(user));
    // when
    Optional<User> userOptional = userRepository.findUserWithId(user.getId());
    // then
    assertTrue(userOptional.isPresent());
    User userFromRepository = userOptional.get();
    assertEquals(user.getId(), userFromRepository.getId());
    assertEquals(user.getEmail(), userFromRepository.getEmail());
  }

  @Test
  void saveUser_shouldPersistUser() {
    // given
    UserRepository userRepository = new InMemoryUserRepository();
    User user =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    // when
    userRepository.saveUser(user);
    // then
    Optional<User> foundUser = userRepository.findUserWithId(user.getId());
    assertTrue(foundUser.isPresent());
    assertEquals(user.getId(), foundUser.get().getId());
    assertEquals(user.getEmail(), foundUser.get().getEmail());
  }

  @Test
  void findUserWithEmail_shouldReturnUser_whenExists() {
    // given
    UserRepository userRepository = new InMemoryUserRepository();
    User user =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    // when
    userRepository.saveUser(user);
    // then
    assertTrue(userRepository.findUserWithEmail(user.getEmail()).isPresent());
  }

  @Test
  void findUserWithEmail_shouldReturnEmpty_whenNotExists() {
    // given
    UserRepository userRepository = new InMemoryUserRepository();
    // when
    Optional<User> user = userRepository.findUserWithEmail(Email.of("notfound@example.com"));
    // then
    assertTrue(user.isEmpty());
  }

  @Test
  void existsWithEmail_shouldReturnTrue_whenExists() {
    // given
    UserRepository userRepository = new InMemoryUserRepository();
    User user =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    // when
    userRepository.saveUser(user);
    // then
    assertTrue(userRepository.existsWithEmail(user.getEmail()));
  }

  @Test
  void existsWithEmail_shouldReturnFalse_whenNotExists() {
    // given
    UserRepository userRepository = new InMemoryUserRepository();
    // when
    boolean condition = userRepository.existsWithEmail(Email.of("notfound@example.com"));
    // then
    assertFalse(condition);
  }

  @Test
  void saveUser_shouldNotOverwriteExistingUser() {
    // given
    User user =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    UserRepository userRepository = new InMemoryUserRepository();
    userRepository.saveUser(user);
    User user2 =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator) {
          @Override
          public UUID getId() {
            return user.getId();
          }
        };
    // when
    userRepository.saveUser(user2);
    Optional<User> found = userRepository.findUserWithId(user.getId());
    assertTrue(found.isPresent());
    assertSame(user, found.get());
  }

  @Test
  void deleteUsers_shouldRemoveAllUsers() {
    // given
    UserRepository userRepository = new InMemoryUserRepository();
    User user1 =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    User user2 =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    userRepository.saveUser(user1);
    userRepository.saveUser(user2);
    // when
    userRepository.deleteUsers();
    // then
    assertTrue(userRepository.findUserWithId(user1.getId()).isEmpty());
    assertTrue(userRepository.findUserWithId(user2.getId()).isEmpty());
    assertFalse(userRepository.existsWithEmail(user1.getEmail()));
    assertFalse(userRepository.existsWithEmail(user2.getEmail()));
    assertTrue(userRepository.findUserWithEmail(user1.getEmail()).isEmpty());
    assertTrue(userRepository.findUserWithEmail(user2.getEmail()).isEmpty());
  }
}
