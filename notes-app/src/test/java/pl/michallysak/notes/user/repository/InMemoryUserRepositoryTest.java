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
  void findById_shouldReturnEmpty_whenNoExists() {
    // given
    UserRepository userRepository = new InMemoryUserRepository();
    UUID randomId = UUID.randomUUID();
    // when
    Optional<User> userOptional = userRepository.findById(randomId);
    // then
    assertTrue(userOptional.isEmpty());
  }

  @Test
  void findById_shouldReturnIt_whenExists() {
    // given
    User user =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    UserRepository userRepository = new InMemoryUserRepository(Collections.singletonList(user));
    // when
    Optional<User> userOptional = userRepository.findById(user.getId());
    // then
    assertTrue(userOptional.isPresent());
    User userFromRepository = userOptional.get();
    assertEquals(user.getId(), userFromRepository.getId());
    assertEquals(user.getEmail(), userFromRepository.getEmail());
  }

  @Test
  void save_shouldPersistUser() {
    // given
    UserRepository userRepository = new InMemoryUserRepository();
    User user =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    // when
    userRepository.save(user);
    // then
    Optional<User> foundUser = userRepository.findById(user.getId());
    assertTrue(foundUser.isPresent());
    assertEquals(user.getId(), foundUser.get().getId());
    assertEquals(user.getEmail(), foundUser.get().getEmail());
  }

  @Test
  void findByEmail_shouldReturnUser_whenExists() {
    // given
    UserRepository userRepository = new InMemoryUserRepository();
    User user =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    // when
    userRepository.save(user);
    // then
    assertTrue(userRepository.findByEmail(user.getEmail()).isPresent());
  }

  @Test
  void findByEmail_shouldReturnEmpty_whenNotExists() {
    // given
    UserRepository userRepository = new InMemoryUserRepository();
    // when
    Optional<User> user = userRepository.findByEmail(Email.of("notfound@example.com"));
    // then
    assertTrue(user.isEmpty());
  }

  @Test
  void existsByEmail_shouldReturnTrue_whenExists() {
    // given
    UserRepository userRepository = new InMemoryUserRepository();
    User user =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    // when
    userRepository.save(user);
    // then
    assertTrue(userRepository.existsByEmail(user.getEmail()));
  }

  @Test
  void existsByEmail_shouldReturnFalse_whenNotExists() {
    // given
    UserRepository userRepository = new InMemoryUserRepository();
    // when
    boolean condition = userRepository.existsByEmail(Email.of("notfound@example.com"));
    // then
    assertFalse(condition);
  }

  @Test
  void save_shouldNotOverwriteExistingUser() {
    // given
    User user =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    UserRepository userRepository = new InMemoryUserRepository();
    userRepository.save(user);
    User user2 =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator) {
          @Override
          public UUID getId() {
            return user.getId();
          }
        };
    // when
    userRepository.save(user2);
    Optional<User> found = userRepository.findById(user.getId());
    assertTrue(found.isPresent());
    assertSame(user, found.get());
  }

  @Test
  void deleteAll_shouldRemoveAllUsers() {
    // given
    UserRepository userRepository = new InMemoryUserRepository();
    User user1 =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    User user2 =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    userRepository.save(user1);
    userRepository.save(user2);
    // when
    userRepository.deleteAll();
    // then
    assertTrue(userRepository.findById(user1.getId()).isEmpty());
    assertTrue(userRepository.findById(user2.getId()).isEmpty());
    assertFalse(userRepository.existsByEmail(user1.getEmail()));
    assertFalse(userRepository.existsByEmail(user2.getEmail()));
  }
}
