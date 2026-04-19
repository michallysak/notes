package pl.michallysak.notes.application.quarkus.user.domain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.quarkus.runtime.configuration.ConfigurationException;
import jakarta.enterprise.inject.Instance;
import java.util.Optional;
import java.util.function.Consumer;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
  private static final String PERSISTENCE = "persistence";

  @Mock Logger logger;
  @Mock PanacheUserRepository panacheUserRepository;
  @Mock Instance<PanacheUserRepository> panacheUserRepositoryInstance;

  @InjectMocks UserBeans userBeans;

  @Test
  void userRepository_shouldReturnInMemoryUserRepository_whenGetPersistenceReturnEmptyString() {
    // when
    UserRepository repo = userBeans.userRepository(panacheUserRepositoryInstance);
    // then
    assertInstanceOf(InMemoryUserRepository.class, repo);
    verifyNoInteractions(panacheUserRepositoryInstance);
  }

  @Test
  void userRepository_shouldReturnInMemoryUserRepository_whenConfigInMemory() {
    withMockedConfigProvider(
        (config) -> {
          // given
          mockPersistenceConfig(config, "in-memory");
          // when
          UserRepository userRepository = userBeans.userRepository(panacheUserRepositoryInstance);
          // then
          assertNotNull(userRepository);
          assertInstanceOf(InMemoryUserRepository.class, userRepository);
          verifyNoInteractions(panacheUserRepositoryInstance);
        });
  }

  @Test
  void userRepository_shouldReturnInMemoryUserRepository_whenNoConfig() {
    withMockedConfigProvider(
        (config) -> {
          // given
          mockPersistenceConfig(config);
          // when
          UserRepository userRepository = userBeans.userRepository(panacheUserRepositoryInstance);
          // then
          assertNotNull(userRepository);
          assertInstanceOf(InMemoryUserRepository.class, userRepository);
          verifyNoInteractions(panacheUserRepositoryInstance);
        });
  }

  @Test
  void userRepository_shouldThrow_whenGetPersistenceReturnUnsupportedPersistence() {
    withMockedConfigProvider(
        (config) -> {
          // given
          String value = "xyz";
          mockPersistenceConfig(config, value);
          // when
          Executable executable = () -> userBeans.userRepository(panacheUserRepositoryInstance);
          // then
          ConfigurationException exception = assertThrows(ConfigurationException.class, executable);
          assertEquals(
              "Unsupported persistence type: \"%s\"".formatted(value), exception.getMessage());
          verifyNoInteractions(panacheUserRepositoryInstance);
        });
  }

  @Test
  void userRepository_shouldReturnPanacheUserRepository_whenConfigPanache() {
    withMockedConfigProvider(
        (config) -> {
          // given
          mockPersistenceConfig(config, "sql");
          when(panacheUserRepositoryInstance.get()).thenReturn(panacheUserRepository);
          // when
          UserRepository userRepository = userBeans.userRepository(panacheUserRepositoryInstance);
          // then
          assertNotNull(userRepository);
          assertSame(panacheUserRepository, userRepository);
          verify(panacheUserRepositoryInstance).get();
        });
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

  private static void mockPersistenceConfig(Config config, String value) {
    when(config.getOptionalValue(PERSISTENCE, String.class)).thenReturn(Optional.ofNullable(value));
  }

  private static void mockPersistenceConfig(Config config) {
    when(config.getOptionalValue(PERSISTENCE, String.class)).thenReturn(Optional.empty());
  }

  private static void withMockedConfigProvider(Consumer<Config> consumer) {
    try (MockedStatic<ConfigProvider> configProviderMock = mockStatic(ConfigProvider.class)) {
      Config config = mock(Config.class);
      configProviderMock.when(ConfigProvider::getConfig).thenReturn(config);
      consumer.accept(config);
    }
  }
}
