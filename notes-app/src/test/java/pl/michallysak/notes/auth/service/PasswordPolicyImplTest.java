package pl.michallysak.notes.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.auth.domain.PBKDF2PasswordCredential;
import pl.michallysak.notes.auth.domain.PasswordCredential;
import pl.michallysak.notes.auth.domain.TestPasswordCredential;
import pl.michallysak.notes.auth.exception.AuthException;
import pl.michallysak.notes.auth.model.PBKDF2HashedPassword;
import pl.michallysak.notes.auth.model.Password;

@ExtendWith(MockitoExtension.class)
class PasswordPolicyImplTest {
  private static final int ITERATIONS = 1000;
  @Mock private PBKDF2Hasher hasher;
  @Mock Logger logger;
  @InjectMocks private PasswordPolicyImpl passwordPolicy;

  @Test
  void isUpToDate_shouldReturnTrue_whenIterationsSufficient() {
    // given
    PBKDF2HashedPassword hashed =
        new PBKDF2HashedPassword(new byte[] {1}, new byte[] {2}, ITERATIONS);
    PBKDF2PasswordCredential credential = new PBKDF2PasswordCredential(hashed);
    // when
    boolean upToDate = passwordPolicy.isUpToDate(credential);
    // then
    assertTrue(upToDate);
  }

  @Test
  void isUpToDate_shouldReturnFalse_whenIterationsInsufficient() {
    // given
    PBKDF2HashedPassword hashed = new PBKDF2HashedPassword(new byte[] {1}, new byte[] {2}, 500);
    when(hasher.getIterations()).thenReturn(ITERATIONS);
    PBKDF2PasswordCredential credential = new PBKDF2PasswordCredential(hashed);
    // when
    boolean upToDate = passwordPolicy.isUpToDate(credential);
    // then
    assertFalse(upToDate);
  }

  @Test
  void isUpToDate_shouldReturnFalse_whenNotPBKDF2Credential() {
    // given
    PasswordCredential notPBKDF2 = new TestPasswordCredential();
    // when
    boolean upToDate = passwordPolicy.isUpToDate(notPBKDF2);
    // then
    assertFalse(upToDate);
  }

  @Test
  void hash_shouldReturnCredential_whenValidPassword() {
    // given
    Password password = Password.of("Test123!@#");
    byte[] salt = new byte[] {1, 2, 3};
    byte[] hash = new byte[] {4, 5, 6};
    when(hasher.getIterations()).thenReturn(ITERATIONS);
    when(hasher.generateSalt()).thenReturn(salt);
    when(hasher.hash(eq(password.getValue().toCharArray()), eq(salt))).thenReturn(hash);
    // when
    PasswordCredential credential = passwordPolicy.hash(password);
    // then
    assertNotNull(credential);
    assertInstanceOf(PBKDF2PasswordCredential.class, credential);
    PBKDF2HashedPassword hashed = ((PBKDF2PasswordCredential) credential).getHashedPassword();
    assertArrayEquals(hash, hashed.getHash());
    assertArrayEquals(salt, hashed.getSalt());
    assertEquals(ITERATIONS, hashed.getIterations());
  }

  @Test
  void verifyPassword_shouldReturnTrue_whenCorrect() {
    // given
    Password password = Password.of("Test123!@#");
    byte[] salt = new byte[] {1, 2, 3};
    byte[] hash = new byte[] {4, 5, 6};
    when(hasher.getIterations()).thenReturn(ITERATIONS);
    PBKDF2HashedPassword stored = new PBKDF2HashedPassword(hash, salt, ITERATIONS);
    PBKDF2PasswordCredential credential = new PBKDF2PasswordCredential(stored);
    when(hasher.hash(eq(password.getValue().toCharArray()), eq(salt))).thenReturn(hash);
    // when
    boolean result = passwordPolicy.verifyPassword(password, credential);
    // then
    assertTrue(result);
  }

  @Test
  void verifyPassword_shouldReturnFalse_whenIncorrect() {
    // given
    Password password = Password.of("Test123!@#");
    byte[] salt = new byte[] {1, 2, 3};
    byte[] hash = new byte[] {4, 5, 6};
    byte[] wrongHash = new byte[] {7, 8, 9};
    when(hasher.getIterations()).thenReturn(ITERATIONS);
    PBKDF2HashedPassword stored = new PBKDF2HashedPassword(hash, salt, ITERATIONS);
    PBKDF2PasswordCredential credential = new PBKDF2PasswordCredential(stored);
    when(hasher.hash(any(), eq(salt))).thenReturn(wrongHash);
    // when
    boolean result = passwordPolicy.verifyPassword(password, credential);
    // then
    assertFalse(result);
  }

  @Test
  void verifyPassword_shouldThrow_whenNotPBKDF2Credential() {
    // given
    Password password = Password.of("Test123!@#");
    PasswordCredential notPBKDF2 = new TestPasswordCredential();
    // when
    Executable executable = () -> passwordPolicy.verifyPassword(password, notPBKDF2);
    // then
    AuthException exception = assertThrows(AuthException.class, executable);
    assertEquals("Invalid credential", exception.getMessage());
  }

  @Test
  void verifyPassword_shouldWork_whenCredentialHasDifferentIterations() {
    // given
    Password password = Password.of("Test123!@#");
    byte[] salt = new byte[] {9, 8, 7};
    byte[] hash = new byte[] {1, 2, 3};
    PBKDF2HashedPassword hashedPassword = mock(PBKDF2HashedPassword.class);
    when(hashedPassword.getSalt()).thenReturn(salt);
    when(hashedPassword.getHash()).thenReturn(hash);
    when(hashedPassword.getIterations()).thenReturn(5000);
    PBKDF2PasswordCredential credential = mock(PBKDF2PasswordCredential.class);
    when(credential.getHashedPassword()).thenReturn(hashedPassword);
    when(hasher.getIterations()).thenReturn(ITERATIONS);
    // when
    passwordPolicy.verifyPassword(password, credential);
    // then
    verify(hashedPassword, times(2)).getIterations();
    verify(hasher).getIterations();
  }
}
