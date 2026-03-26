package pl.michallysak.notes.auth.service;

import java.security.MessageDigest;
import org.jboss.logging.Logger;
import pl.michallysak.notes.auth.domain.PBKDF2PasswordCredential;
import pl.michallysak.notes.auth.domain.PasswordCredential;
import pl.michallysak.notes.auth.exception.AuthException;
import pl.michallysak.notes.auth.model.PBKDF2HashedPassword;
import pl.michallysak.notes.auth.model.Password;

public class PasswordPolicyImpl implements PasswordPolicy {

  private final Logger logger;
  private final PBKDF2Hasher pbkdf2Hasher;

  public PasswordPolicyImpl(PBKDF2Hasher pbkdf2Hasher, Logger logger) {
    this.pbkdf2Hasher = pbkdf2Hasher;
    this.logger = logger;
  }

  @Override
  public boolean isUpToDate(PasswordCredential credential) {
    if (credential instanceof PBKDF2PasswordCredential pbkdf2) {
      boolean needRehash =
          pbkdf2.getHashedPassword().getIterations() >= pbkdf2Hasher.getIterations();
      logger.info(
          "PBKDF2PasswordCredential with id: %s isUpToDate: %s"
              .formatted(pbkdf2.getId(), needRehash));
      return needRehash;
    }
    logger.info("Unsupported credential type: %s".formatted(credential.getClass().getName()));
    return false;
  }

  @Override
  public PasswordCredential hash(Password password) {
    byte[] salt = pbkdf2Hasher.generateSalt();
    byte[] hash = pbkdf2Hasher.hash(password.getValue().toCharArray(), salt);

    PBKDF2HashedPassword hashedPassword =
        new PBKDF2HashedPassword(hash, salt, pbkdf2Hasher.getIterations());
    return new PBKDF2PasswordCredential(hashedPassword);
  }

  @Override
  public boolean verifyPassword(Password password, PasswordCredential credential) {
    if (!(credential instanceof PBKDF2PasswordCredential pbkdf2)) {
      logger.info("Unsupported credential type: %s".formatted(credential.getClass().getName()));
      throw new AuthException("Invalid credential");
    }

    PBKDF2HashedPassword hashedPassword = pbkdf2.getHashedPassword();
    PBKDF2Hasher hasher = pbkdf2Hasher;
    if (hashedPassword.getIterations() != pbkdf2Hasher.getIterations()) {
      hasher = new PBKDF2Hasher(hashedPassword.getIterations());
    }

    byte[] hash = hasher.hash(password.getValue().toCharArray(), hashedPassword.getSalt());

    return MessageDigest.isEqual(hash, hashedPassword.getHash());
  }
}
