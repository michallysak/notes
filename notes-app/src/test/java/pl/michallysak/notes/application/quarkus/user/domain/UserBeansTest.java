package pl.michallysak.notes.application.quarkus.user.domain;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.auth.service.*;
import pl.michallysak.notes.auth.validator.PasswordStrengthValidator;
import pl.michallysak.notes.auth.validator.PasswordStrengthValidatorImpl;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.repository.InMemoryUserRepository;
import pl.michallysak.notes.user.repository.UserRepository;
import pl.michallysak.notes.user.service.UserService;
import pl.michallysak.notes.user.service.UserServiceImpl;
import pl.michallysak.notes.user.validator.UserValidator;
import pl.michallysak.notes.user.validator.UserValidatorImpl;

@ExtendWith(MockitoExtension.class)
class UserBeansTest {

  @Mock Logger logger;

  @InjectMocks UserBeans userBeans;

  @Test
  void userRepository_shouldReturnInMemoryUserRepository() {
    // when
    UserRepository repo = userBeans.userRepository();
    // then
    assertInstanceOf(InMemoryUserRepository.class, repo);
  }

  @Test
  void passwordStrengthValidator_shouldReturnPasswordStrengthValidatorImpl() {
    // when
    PasswordStrengthValidator validator = userBeans.passwordStrengthValidator();
    // then
    assertInstanceOf(PasswordStrengthValidatorImpl.class, validator);
  }

  @Test
  void userValidator_shouldReturnUserValidatorImpl() {
    // given
    PasswordStrengthValidator passwordStrengthValidator = mock(PasswordStrengthValidator.class);
    // when
    UserValidator validator = userBeans.userValidator(passwordStrengthValidator);
    // then
    assertInstanceOf(UserValidatorImpl.class, validator);
  }

  @Test
  void pbkdf2Hasher_shouldReturnPBKDF2Hasher() {
    // when
    PBKDF2Hasher hasher = userBeans.pbkdf2Hasher();
    // then
    assertInstanceOf(PBKDF2Hasher.class, hasher);
  }

  @Test
  void passwordPolicy_shouldReturnPasswordPolicyImpl_withMock() {
    // given
    PBKDF2Hasher hasher = mock(PBKDF2Hasher.class);
    // when
    PasswordPolicy policy = userBeans.passwordPolicy(hasher);
    // then
    assertInstanceOf(PasswordPolicyImpl.class, policy);
  }

  @Test
  void userService_shouldReturnUserServiceImpl_withMocks() {
    // given
    Logger logger = mock(Logger.class);
    UserRepository userRepository = mock(UserRepository.class);
    UserValidator userValidator = mock(UserValidator.class);
    @SuppressWarnings("unchecked")
    AuthTokenGenerator<UserValue, AuthToken> jwtAuthGenerator = mock(AuthTokenGenerator.class);
    PasswordPolicy passwordPolicy = mock(PasswordPolicy.class);
    // when
    UserService service =
        userBeans.userService(
            logger, userRepository, userValidator, jwtAuthGenerator, passwordPolicy);
    // then
    assertInstanceOf(UserServiceImpl.class, service);
  }
}
