package pl.michallysak.notes.auth.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.auth.exception.AuthException;

@RequiredArgsConstructor
public class PBKDF2Hasher implements PasswordHasher {

  private static final int KEY_LENGTH = 256;
  @Getter private final int iterations;

  @Override
  public byte[] hash(char[] password, byte[] salt) {
    try {
      PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
      SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      return skf.generateSecret(spec).getEncoded();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new AuthException("Credentials invalid", e);
    }
  }

  @Override
  public byte[] generateSalt() {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[32];
    random.nextBytes(salt);
    return salt;
  }
}
