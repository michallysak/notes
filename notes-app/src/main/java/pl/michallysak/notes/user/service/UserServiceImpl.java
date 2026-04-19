package pl.michallysak.notes.user.service;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import pl.michallysak.notes.auth.domain.PasswordCredential;
import pl.michallysak.notes.auth.exception.AuthException;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.auth.model.Password;
import pl.michallysak.notes.auth.service.AuthTokenGenerator;
import pl.michallysak.notes.auth.service.PasswordPolicy;
import pl.michallysak.notes.user.domain.User;
import pl.michallysak.notes.user.domain.UserImpl;
import pl.michallysak.notes.user.exception.UserNotFoundException;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.repository.UserRepository;
import pl.michallysak.notes.user.validator.UserValidator;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final Logger logger;
  private final UserRepository userRepository;
  private final UserValidator userValidator;
  private final AuthTokenGenerator<UserValue, AuthToken> tokenGenerator;
  private final PasswordPolicy passwordPolicy;

  @Override
  public UserValue createUser(EmailPasswordCreateUser createUser) {
    if (userRepository.existsWithEmail(createUser.email())) {
      logger.debug("Attempt to create user with existing email %s".formatted(createUser.email()));
      throw new AuthException("Email already in use");
    }

    User user = new UserImpl(createUser, userValidator);

    saveUserPassword(user, createUser.password());

    return UserValue.from(user);
  }

  @Override
  public AuthToken login(EmailPasswordLogin login) {
    Objects.requireNonNull(login);
    Objects.requireNonNull(login.email());
    Objects.requireNonNull(login.password());

    User user =
        userRepository
            .findUserWithEmail(login.email())
            .orElseThrow(
                () -> {
                  logger.info("User not found with email %s".formatted(login.email()));
                  return new AuthException("Invalid credentials");
                });

    PasswordCredential latest =
        user.getLatestCredential(PasswordCredential.class)
            .orElseThrow(
                () -> {
                  logger.info("No password credential found for user %s".formatted(user.getId()));
                  return new AuthException("Invalid credentials");
                });

    Password password = login.password();

    if (!passwordPolicy.verifyPassword(password, latest)) {
      logger.info("Invalid password for user %s".formatted(user.getId()));
      throw new AuthException("Invalid credentials");
    }

    if (!passwordPolicy.isUpToDate(latest)) {
      logger.info("Migrate password hash for user %s".formatted(user.getId()));
      saveUserPassword(user, password);
    }

    return tokenGenerator.generateToken(UserValue.from(user));
  }

  @Override
  public UserValue getUser(UUID userId) {
    User user = userRepository.findUserWithId(userId).orElseThrow(UserNotFoundException::new);
    return UserValue.from(user);
  }

  private void saveUserPassword(User user, Password createUser) {
    PasswordCredential credential = passwordPolicy.hash(createUser);
    user.deleteCredentials(PasswordCredential.class);
    user.addCredential(credential);
    userRepository.saveUser(user);
  }
}
