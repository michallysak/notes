package pl.michallysak.notes.application.quarkus.user.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import pl.michallysak.notes.application.quarkus.user.dto.AuthTokenResponse;
import pl.michallysak.notes.application.quarkus.user.dto.LoginUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.RegisterUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.UserResponse;
import pl.michallysak.notes.auth.domain.PBKDF2PasswordCredential;
import pl.michallysak.notes.auth.domain.PasswordCredential;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.auth.model.PBKDF2HashedPassword;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.domain.User;
import pl.michallysak.notes.user.domain.UserImpl;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.repository.UserCredentialEntity;
import pl.michallysak.notes.user.repository.UserEntity;
import pl.michallysak.notes.user.validator.UserValidator;

class UserMapperTest {
  private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final String ENCODED_PASSWORD = PBKDF2_ALGORITHM + "$5000$Ag==$AQ==";
  private static final String OLDER_CREDENTIAL_PASSWORD = PBKDF2_ALGORITHM + "$5000$AQ==$Ag==";
  private static final String NEWER_CREDENTIAL_PASSWORD = PBKDF2_ALGORITHM + "$5000$AwQ=$AQI=";
  private static final String UNSUPPORTED_ALGORITHM_PASSWORD = "argon2$3$AwQ=$AQI=";

  private final CredentialMapper credentialMapper = mock(CredentialMapper.class);
  private final UserValidator userValidator = mock(UserValidator.class);
  private UserMapper mapper;

  @BeforeEach
  void setUp() {
    UserMapperImpl mapper = new UserMapperImpl();
    mapper.setUserValidator(userValidator);
    mapper.setCredentialMapper(credentialMapper);
    this.mapper = mapper;
  }

  @Test
  void mapToEmailPasswordLogin_fromLoginUserRequest() {
    // given
    LoginUserRequest request =
        LoginUserRequest.builder().email("test@example.com").password("Password123!").build();
    // when
    EmailPasswordLogin result = mapper.mapToEmailPasswordLogin(request);
    // then
    assertEquals("test@example.com", result.email().getValue());
    assertEquals("Password123!", result.password().getValue());
  }

  @Test
  void mapToEmailPasswordLogin_fromRegisterUserRequest() {
    // given
    RegisterUserRequest request =
        RegisterUserRequest.builder().email("test@example.com").password("Password123!").build();
    // when
    EmailPasswordLogin result = mapper.mapToEmailPasswordLogin(request);
    // then
    assertEquals("test@example.com", result.email().getValue());
    assertEquals("Password123!", result.password().getValue());
  }

  @Test
  void mapToEmailPasswordCreateUser_fromRegisterUserRequest() {
    // given
    RegisterUserRequest request =
        RegisterUserRequest.builder().email("test@example.com").password("Password123!").build();
    // when
    EmailPasswordCreateUser result = mapper.mapToEmailPasswordCreateUser(request);
    // then
    assertEquals("test@example.com", result.email().getValue());
    assertEquals("Password123!", result.password().getValue());
  }

  @Test
  void mapToUserResponse_shouldMapFields() {
    // given
    UserValue userValue =
        UserValue.builder()
            .id(java.util.UUID.randomUUID())
            .email(Email.of("test@example.com"))
            .build();
    // when
    UserResponse response = mapper.mapToUserResponse(userValue);
    // then
    assertEquals("test@example.com", response.getEmail());
  }

  @Test
  void mapToAuthTokenResponse_shouldMapFields() {
    // given
    AuthToken token = new AuthToken("token", OffsetDateTime.now().plusHours(1));
    // when
    AuthTokenResponse response = mapper.mapToAuthTokenResponse(token);
    // then
    assertEquals("token", response.getToken());
  }

  @Test
  void mapToEmailPasswordLogin_fromLoginUserRequest_nullInput() {
    // given
    LoginUserRequest request = null;
    // when
    EmailPasswordLogin result = mapper.mapToEmailPasswordLogin(request);
    // then
    assertNull(result);
  }

  @Test
  void mapToEmailPasswordLogin_fromRegisterUserRequest_nullInput() {
    // given
    RegisterUserRequest request = null;
    // when
    EmailPasswordLogin result = mapper.mapToEmailPasswordLogin(request);
    // then
    assertNull(result);
  }

  @Test
  void mapToEmailPasswordCreateUser_fromRegisterUserRequest_nullInput() {
    // given
    RegisterUserRequest request = null;
    // when
    EmailPasswordCreateUser result = mapper.mapToEmailPasswordCreateUser(request);
    // then
    assertNull(result);
  }

  @Test
  void mapToUserResponse_nullInput() {
    // given
    UserValue userValue = null;
    // when
    UserResponse result = mapper.mapToUserResponse(userValue);
    // then
    assertNull(result);
  }

  @Test
  void mapToAuthTokenResponse_nullInput() {
    // given
    AuthToken token = null;
    // when
    AuthTokenResponse result = mapper.mapToAuthTokenResponse(token);
    // then
    assertNull(result);
  }

  @Test
  void mapToEntity_shouldMapUserWithPasswordCredential() {
    // given
    UUID userId = UUID.randomUUID();
    Email email = Email.of("test@example.com");
    OffsetDateTime createdAt = OffsetDateTime.now();
    PBKDF2HashedPassword hashedPassword =
        new PBKDF2HashedPassword(new byte[] {1}, new byte[] {2}, 5000);
    PBKDF2PasswordCredential credential =
        new PBKDF2PasswordCredential(UUID.randomUUID(), createdAt, hashedPassword);
    UserValue build = UserValue.builder().id(userId).email(email).build();
    User user = new UserImpl(build, List.of(credential), mock(UserValidator.class));

    UserCredentialEntity credentialEntity = new UserCredentialEntity();
    credentialEntity.setId(UUID.randomUUID());
    credentialEntity.setCreated(createdAt);
    credentialEntity.setValue(ENCODED_PASSWORD);
    when(credentialMapper.mapToEntities(user)).thenReturn(List.of(credentialEntity));

    // when
    UserEntity entity = mapper.mapToEntity(user);
    // then
    assertEquals(userId, entity.getId());
    assertEquals(email.getValue(), entity.getEmail());
    assertEquals(createdAt, entity.getCreated());
    assertEquals(1, entity.getCredentials().size());
    UserCredentialEntity resultCredential = entity.getCredentials().getFirst();
    assertEquals(createdAt, resultCredential.getCreated());
    assertEquals(ENCODED_PASSWORD, resultCredential.getValue());
    assertEquals(entity, resultCredential.getUser());
  }

  @Test
  void mapToDomain_shouldMapEntityWithCredentials() {
    // given
    OffsetDateTime now = OffsetDateTime.now();
    UserEntity entity = new UserEntity();
    entity.setId(UUID.randomUUID());
    entity.setEmail("test@example.com");

    UserCredentialEntity olderCredential = new UserCredentialEntity();
    olderCredential.setId(UUID.randomUUID());
    olderCredential.setCreated(now.minusDays(1));
    olderCredential.setValue(OLDER_CREDENTIAL_PASSWORD);

    UserCredentialEntity newerCredential = new UserCredentialEntity();
    newerCredential.setId(UUID.randomUUID());
    newerCredential.setCreated(now);
    newerCredential.setValue(NEWER_CREDENTIAL_PASSWORD);

    entity.setCredentials(java.util.List.of(newerCredential, olderCredential));
    entity.setCreated(olderCredential.getCreated());

    PBKDF2HashedPassword olderHashed =
        new PBKDF2HashedPassword(new byte[] {0, 1}, new byte[] {2, 3}, 5000);
    PBKDF2PasswordCredential olderCred =
        new PBKDF2PasswordCredential(
            olderCredential.getId(), olderCredential.getCreated(), olderHashed);

    PBKDF2HashedPassword newerHashed =
        new PBKDF2HashedPassword(new byte[] {1, 2}, new byte[] {3, 4}, 5000);
    PBKDF2PasswordCredential newerCred =
        new PBKDF2PasswordCredential(
            newerCredential.getId(), newerCredential.getCreated(), newerHashed);

    when(credentialMapper.mapToDomain(entity)).thenReturn(List.of(olderCred, newerCred));

    // when
    User user = mapper.mapToDomain(entity);
    // then
    assertEquals(entity.getId(), user.getId());
    assertEquals(Email.of(entity.getEmail()), user.getEmail());
    assertEquals(2, user.getCredentials(PasswordCredential.class).size());
    PasswordCredential credential =
        user.getLatestCredential(PasswordCredential.class).orElseThrow();
    assertEquals(now, credential.getCreatedAt());
    assertArrayEquals(new byte[] {1, 2}, credential.getHashedPassword().getHash());
    assertArrayEquals(new byte[] {3, 4}, credential.getHashedPassword().getSalt());
    if (credential instanceof PBKDF2PasswordCredential pbkdf2Credential) {
      assertEquals(5000, pbkdf2Credential.getHashedPassword().getIterations());
    } else {
      fail("Expected credential to be an instance of PBKDF2PasswordCredential");
    }
  }

  @Test
  void decodePassword_shouldThrow_whenAlgorithmUnsupported() {
    // given
    UserEntity entity = new UserEntity();
    entity.setId(UUID.randomUUID());
    entity.setEmail("test@example.com");
    UserCredentialEntity credential = new UserCredentialEntity();
    credential.setValue(UNSUPPORTED_ALGORITHM_PASSWORD);
    entity.setCredentials(List.of(credential));
    when(credentialMapper.mapToDomain(entity))
        .thenThrow(new IllegalStateException("Unsupported password algorithm: argon2"));
    // when
    Executable executable = () -> mapper.mapToDomain(entity);
    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, executable);
    assertEquals("Unsupported password algorithm: argon2", exception.getMessage());
  }

  @Test
  void mapToEntity_shouldThrow_whenUserHasNoPasswordCredential() {
    // given
    UUID userId = UUID.randomUUID();
    Email email = Email.of("test@example.com");
    UserValue build = UserValue.builder().id(userId).email(email).build();
    User user = new UserImpl(build, List.of(), mock(UserValidator.class));
    // when
    Executable executable = () -> mapper.mapToEntity(user);
    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, executable);
    assertEquals("User has no password credential", exception.getMessage());
  }

  @Test
  void getCreated_shouldThrow_whenCredentialsListIsEmpty() {
    // given
    UUID userId = UUID.randomUUID();
    Email email = Email.of("test@example.com");
    OffsetDateTime createdAt = OffsetDateTime.now();
    PBKDF2HashedPassword hashedPassword =
        new PBKDF2HashedPassword(new byte[] {1}, new byte[] {2}, 5000);
    PBKDF2PasswordCredential credential =
        new PBKDF2PasswordCredential(UUID.randomUUID(), createdAt, hashedPassword);
    UserValue build = UserValue.builder().id(userId).email(email).build();
    User user = new UserImpl(build, List.of(credential), mock(UserValidator.class));
    when(credentialMapper.mapToEntities(user)).thenReturn(List.of());
    // when
    Executable executable = () -> mapper.mapToEntity(user);
    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, executable);
    assertEquals("User has no password credential", exception.getMessage());
  }
}
