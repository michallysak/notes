package pl.michallysak.notes.application.quarkus.user.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import pl.michallysak.notes.auth.domain.Credential;
import pl.michallysak.notes.auth.domain.PBKDF2PasswordCredential;
import pl.michallysak.notes.auth.domain.PasswordCredential;
import pl.michallysak.notes.auth.model.PBKDF2HashedPassword;
import pl.michallysak.notes.user.domain.User;
import pl.michallysak.notes.user.repository.UserCredentialEntity;
import pl.michallysak.notes.user.repository.UserEntity;

class CredentialMapperTest {
  private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
  private static final String ENCODED_PASSWORD = PBKDF2_ALGORITHM + "$5000$Ag==$AQ==";
  private static final String DECODED_PASSWORD = PBKDF2_ALGORITHM + "$5000$AwQ=$AQI=";
  private static final String UNSUPPORTED_ALGORITHM_PASSWORD = "argon2$3$AwQ=$AQI=";
  private static final String INVALID_FORMAT_PASSWORD = "invalid$format";

  private final CredentialMapper credentialMapper = new CredentialMapper();

  @Test
  void encodePassword_shouldEncodeToExpectedFormat() {
    // given
    PBKDF2HashedPassword hashedPassword =
        new PBKDF2HashedPassword(new byte[] {1}, new byte[] {2}, 5000);
    PasswordCredential credential =
        new PBKDF2PasswordCredential(UUID.randomUUID(), OffsetDateTime.now(), hashedPassword);
    // when
    User user = mock(User.class);
    when(user.getCredentials(PasswordCredential.class)).thenReturn(List.of(credential));
    List<UserCredentialEntity> entities = credentialMapper.mapToEntities(user);
    // then
    assertEquals(1, entities.size());
    assertEquals(ENCODED_PASSWORD, entities.getFirst().getValue());
  }

  @Test
  void decodeCredential_shouldDecodeEntityPayload() {
    // given
    OffsetDateTime created = OffsetDateTime.now();
    UserCredentialEntity entity = new UserCredentialEntity();
    entity.setId(UUID.randomUUID());
    entity.setCreated(created);
    entity.setValue(DECODED_PASSWORD);
    // when
    UserEntity userEntity = new UserEntity();
    userEntity.setCredentials(List.of(entity));
    List<Credential> credentials = credentialMapper.mapToDomain(userEntity);
    // then
    PBKDF2PasswordCredential credential = (PBKDF2PasswordCredential) credentials.getFirst();
    assertEquals(created, credential.getCreatedAt());
    assertArrayEquals(new byte[] {1, 2}, credential.getHashedPassword().getHash());
    assertArrayEquals(new byte[] {3, 4}, credential.getHashedPassword().getSalt());
    assertEquals(5000, credential.getHashedPassword().getIterations());
  }

  @Test
  void decodePassword_shouldThrow_whenAlgorithmUnsupported() {
    // given
    UserCredentialEntity entity = new UserCredentialEntity();
    entity.setValue(UNSUPPORTED_ALGORITHM_PASSWORD);
    UserEntity userEntity = new UserEntity();
    userEntity.setCredentials(List.of(entity));
    // when
    Executable executable = () -> credentialMapper.mapToDomain(userEntity);
    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, executable);
    assertEquals("Unsupported password algorithm: argon2", exception.getMessage());
  }

  @Test
  void mapToEntities_shouldThrow_whenUserHasNoPasswordCredential() {
    // given
    User user = mock(User.class);
    when(user.getCredentials(PasswordCredential.class)).thenReturn(List.of());

    // when
    Executable executable = () -> credentialMapper.mapToEntities(user);
    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, executable);
    assertEquals("User has no password credential", exception.getMessage());
  }

  @Test
  void decodePassword_shouldThrow_whenPasswordIsNull() {
    // given
    UserCredentialEntity entity = new UserCredentialEntity();
    entity.setValue(null);
    UserEntity userEntity = new UserEntity();
    userEntity.setCredentials(List.of(entity));
    // when
    Executable executable = () -> credentialMapper.mapToDomain(userEntity);
    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, executable);
    assertEquals("User password is missing", exception.getMessage());
  }

  @Test
  void decodePassword_shouldThrow_whenPasswordIsBlank() {
    // given
    UserCredentialEntity entity = new UserCredentialEntity();
    entity.setValue("");
    UserEntity userEntity = new UserEntity();
    userEntity.setCredentials(List.of(entity));
    // when
    Executable executable = () -> credentialMapper.mapToDomain(userEntity);
    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, executable);
    assertEquals("User password is missing", exception.getMessage());
  }

  @Test
  void decodePassword_shouldThrow_whenPasswordFormatIsInvalid() {
    // given
    UserCredentialEntity entity = new UserCredentialEntity();
    entity.setValue(INVALID_FORMAT_PASSWORD);
    UserEntity userEntity = new UserEntity();
    userEntity.setCredentials(List.of(entity));
    // when
    Executable executable = () -> credentialMapper.mapToDomain(userEntity);
    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, executable);
    assertEquals("Unsupported persisted password format", exception.getMessage());
  }

  @Test
  void requirePbkdf2Credential_shouldThrow_whenUnsupportedCredentialType() {
    // given
    PasswordCredential unsupportedCredential = mock(PasswordCredential.class);
    User user = mock(User.class);
    when(user.getCredentials(PasswordCredential.class)).thenReturn(List.of(unsupportedCredential));
    // when
    Executable executable = () -> credentialMapper.mapToEntities(user);
    // then
    IllegalStateException exception = assertThrows(IllegalStateException.class, executable);
    String expected = "Unsupported password credential: " + unsupportedCredential.getClass();
    assertEquals(expected, exception.getMessage());
  }
}
