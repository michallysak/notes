package pl.michallysak.notes.auth.service;

import static org.junit.jupiter.api.Assertions.*;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pl.michallysak.notes.auth.exception.AuthException;

class PBKDF2HasherTest {

  private static final int PBKDF2_BYTE_LENGTH = 32;
  int iterations = 1000;

  @Test
  void generateSalt_shouldWork() {
    // given
    PBKDF2Hasher hasher = new PBKDF2Hasher(iterations);
    // when
    byte[] salt1 = hasher.generateSalt();
    byte[] salt2 = hasher.generateSalt();
    // then
    assertNotNull(salt1);
    assertNotNull(salt2);
    assertEquals(PBKDF2_BYTE_LENGTH, salt1.length);
    assertEquals(PBKDF2_BYTE_LENGTH, salt2.length);
    assertFalse(Arrays.equals(salt1, salt2));
  }

  @Test
  void hash_shouldWork() {
    // given
    PBKDF2Hasher hasher = new PBKDF2Hasher(iterations);
    char[] charArray = "password".toCharArray();
    byte[] salt1 = hasher.generateSalt();
    byte[] salt2 = hasher.generateSalt();
    // when
    byte[] hash1 = hasher.hash(charArray, salt1);
    byte[] hash2 = hasher.hash(charArray, salt1);
    byte[] hash3 = hasher.hash(charArray, salt2);
    // then
    assertNotNull(hash1);
    assertNotNull(hash2);
    assertEquals(PBKDF2_BYTE_LENGTH, hash1.length);
    assertEquals(PBKDF2_BYTE_LENGTH, hash2.length);
    assertEquals(PBKDF2_BYTE_LENGTH, hash3.length);
    assertArrayEquals(hash1, hash2);
    assertFalse(Arrays.equals(hash2, hash3));
  }

  @Test
  void hash_shouldThrow_whenInvalidAlgorithm() {
    try (MockedStatic<SecretKeyFactory> mocked = Mockito.mockStatic(SecretKeyFactory.class)) {
      mocked
          .when(() -> SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"))
          .thenThrow(new NoSuchAlgorithmException("Invalid algorithm"));
      // given
      PBKDF2Hasher hasher = new PBKDF2Hasher(iterations);
      // when
      Executable executable = () -> hasher.hash(new char[0], new byte[PBKDF2_BYTE_LENGTH]);
      // then
      AuthException exception = assertThrows(AuthException.class, executable);
      assertTrue(exception.getMessage().contains("Credentials invalid"));
    }
  }
}
