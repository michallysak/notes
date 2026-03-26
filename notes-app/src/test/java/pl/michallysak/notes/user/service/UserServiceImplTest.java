package pl.michallysak.notes.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.auth.domain.PBKDF2PasswordCredential;
import pl.michallysak.notes.auth.domain.PasswordCredential;
import pl.michallysak.notes.auth.exception.AuthException;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.auth.model.PBKDF2HashedPassword;
import pl.michallysak.notes.auth.model.Password;
import pl.michallysak.notes.auth.service.AuthTokenGenerator;
import pl.michallysak.notes.auth.service.PasswordPolicy;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.UserTestUtils;
import pl.michallysak.notes.user.domain.User;
import pl.michallysak.notes.user.domain.UserImpl;
import pl.michallysak.notes.user.exception.UserNotFoundException;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.repository.UserRepository;
import pl.michallysak.notes.user.validator.UserValidator;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
  @Mock private UserRepository userRepository;
  @Mock private UserValidator userValidator;
  @Mock private Logger logger;
  @Mock private PasswordPolicy passwordPolicy;
  @Mock private AuthTokenGenerator<UserValue, AuthToken> tokenGenerator;
  @InjectMocks private UserServiceImpl userService;

  @Test
  void createUser_shouldSaveAndReturnValue() {
    // given
    EmailPasswordCreateUser emailPasswordCreateUser =
        UserTestUtils.createEmailPasswordCreateUserBuilder().build();
    PasswordCredential credential = getDummyPasswordCredential();
    when(passwordPolicy.hash(any())).thenReturn(credential);
    // when
    UserValue userValue = userService.createUser(emailPasswordCreateUser);
    // then
    verify(userRepository).save(any());
    assertNotNull(userValue.id());
    assertEquals(emailPasswordCreateUser.email(), userValue.email());
  }

  @Test
  void getUser_shouldReturnMappedValue() {
    // given
    User user =
        new UserImpl(UserTestUtils.createEmailPasswordCreateUserBuilder().build(), userValidator);
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    // when
    UserValue value = userService.getUser(user.getId());
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
    Executable executable = () -> userService.getUser(id);
    // then
    assertThrows(UserNotFoundException.class, executable);
  }

  @Test
  void createUser_shouldThrow_whenEmailAlreadyExists() {
    // given
    EmailPasswordCreateUser emailPasswordCreateUser =
        UserTestUtils.createEmailPasswordCreateUserBuilder().build();
    when(userRepository.existsByEmail(emailPasswordCreateUser.email())).thenReturn(true);
    // when
    Executable executable = () -> userService.createUser(emailPasswordCreateUser);
    // then
    AuthException exception = assertThrows(AuthException.class, executable);
    assertEquals("Email already in use", exception.getMessage());
  }

  @Test
  void login_shouldThrow_whenLoginIsNull() {
    // given
    EmailPasswordLogin login = null;
    // when
    Executable executable = () -> userService.login(login);
    // then
    assertThrows(NullPointerException.class, executable);
  }

  @Test
  void login_shouldThrow_whenEmailIsNull() {
    // given
    EmailPasswordLogin login = new EmailPasswordLogin(null, Password.of("pw"));
    // when
    Executable executable = () -> userService.login(login);
    // then
    assertThrows(NullPointerException.class, executable);
  }

  @Test
  void login_shouldThrow_whenPasswordIsNull() {
    // given
    EmailPasswordLogin login = new EmailPasswordLogin(Email.of("user@example.com"), null);
    // when
    Executable executable = () -> userService.login(login);
    // then
    assertThrows(NullPointerException.class, executable);
  }

  @Test
  void login_shouldThrow_whenUserNotFound() {
    // given
    EmailPasswordLogin login =
        new EmailPasswordLogin(
            pl.michallysak.notes.common.Email.of("notfound@example.com"),
            pl.michallysak.notes.auth.model.Password.of("pw"));
    when(userRepository.findByEmail(login.email())).thenReturn(Optional.empty());
    // when
    Executable executable = () -> userService.login(login);
    // then
    AuthException exception = assertThrows(AuthException.class, executable);
    assertEquals("Invalid credentials", exception.getMessage());
  }

  @Test
  void login_shouldThrow_whenNoPasswordCredential() {
    // given
    EmailPasswordLogin login =
        new EmailPasswordLogin(
            pl.michallysak.notes.common.Email.of("user@example.com"),
            pl.michallysak.notes.auth.model.Password.of("pw"));
    User user = mock(User.class);
    when(userRepository.findByEmail(login.email())).thenReturn(Optional.of(user));
    when(user.getLatestCredential(any())).thenReturn(Optional.empty());
    // when
    Executable executable = () -> userService.login(login);
    // then
    AuthException exception = assertThrows(AuthException.class, executable);
    assertEquals("Invalid credentials", exception.getMessage());
  }

  @Test
  void login_shouldThrow_whenPasswordInvalid() {
    // given
    EmailPasswordLogin login =
        new EmailPasswordLogin(
            Email.of("user@example.com"), pl.michallysak.notes.auth.model.Password.of("pw"));
    User user = mock(User.class);
    PasswordCredential credential = getDummyPasswordCredential();
    when(userRepository.findByEmail(login.email())).thenReturn(Optional.of(user));
    when(user.getLatestCredential(any())).thenReturn(Optional.of(credential));
    when(passwordPolicy.verifyPassword(any(), any())).thenReturn(false);
    // when
    Executable executable = () -> userService.login(login);
    // then
    AuthException exception = assertThrows(AuthException.class, executable);
    assertEquals("Invalid credentials", exception.getMessage());
  }

  @Test
  void login_shouldReturnToken_whenPasswordValidAndUpToDate() {
    // given
    EmailPasswordLogin login =
        new EmailPasswordLogin(
            Email.of("user@example.com"), pl.michallysak.notes.auth.model.Password.of("pw"));
    User user = mock(User.class);
    PasswordCredential credential = getDummyPasswordCredential();
    AuthToken token = new AuthToken("token", OffsetDateTime.now().plusHours(1));
    when(userRepository.findByEmail(login.email())).thenReturn(Optional.of(user));
    when(user.getLatestCredential(any())).thenReturn(Optional.of(credential));
    when(passwordPolicy.verifyPassword(any(), any())).thenReturn(true);
    when(passwordPolicy.isUpToDate(any())).thenReturn(true);
    when(tokenGenerator.generateToken(any())).thenReturn(token);
    // when
    AuthToken result = userService.login(login);
    // then
    assertEquals(token, result);
    verify(userRepository, never()).save(any());
  }

  @Test
  void login_shouldMigratePassword_whenPasswordValidAndNotUpToDate() {
    // given
    EmailPasswordLogin login =
        new EmailPasswordLogin(Email.of("user@example.com"), Password.of("pw"));
    User user = mock(User.class);
    PasswordCredential credential = getDummyPasswordCredential();
    AuthToken token = new AuthToken("token", OffsetDateTime.now().plusHours(1));
    when(userRepository.findByEmail(login.email())).thenReturn(Optional.of(user));
    when(user.getLatestCredential(any())).thenReturn(Optional.of(credential));
    when(passwordPolicy.verifyPassword(any(), any())).thenReturn(true);
    when(passwordPolicy.isUpToDate(any())).thenReturn(false);
    when(passwordPolicy.hash(any())).thenReturn(credential);
    when(tokenGenerator.generateToken(any())).thenReturn(token);
    // when
    AuthToken result = userService.login(login);
    // then
    assertEquals(token, result);
    verify(user).deleteCredentials(PasswordCredential.class);
    verify(user).addCredential(credential);
    verify(userRepository).save(user);
  }

  private static PasswordCredential getDummyPasswordCredential() {
    byte[] hash = new byte[] {1, 2, 3};
    byte[] salt = new byte[] {4, 5, 6};
    int iterations = 1000;
    PBKDF2HashedPassword hashedPassword = new PBKDF2HashedPassword(hash, salt, iterations);
    return new PBKDF2PasswordCredential(hashedPassword);
  }
}
