package pl.michallysak.notes.application.quarkus.user.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import pl.michallysak.notes.auth.domain.Credential;
import pl.michallysak.notes.auth.domain.PBKDF2PasswordCredential;
import pl.michallysak.notes.auth.domain.PasswordCredential;
import pl.michallysak.notes.auth.model.PBKDF2HashedPassword;
import pl.michallysak.notes.user.domain.User;
import pl.michallysak.notes.user.repository.UserCredentialEntity;
import pl.michallysak.notes.user.repository.UserEntity;

@ApplicationScoped
public class CredentialMapper {
  private static final String PBKDF2_WITH_HMAC_SHA256 = "PBKDF2WithHmacSHA256";

  public List<UserCredentialEntity> mapToEntities(User user) {
    List<PasswordCredential> credentials = user.getCredentials(PasswordCredential.class);
    if (credentials.isEmpty()) {
      throw new IllegalStateException("User has no password credential");
    }

    return credentials.stream().map(this::mapToEntity).toList();
  }

  private UserCredentialEntity mapToEntity(PasswordCredential credential) {
    UserCredentialEntity credentialEntity = new UserCredentialEntity();
    credentialEntity.setCreated(credential.getCreatedAt());
    credentialEntity.setValue(encodePassword(credential));
    return credentialEntity;
  }

  private String encodePassword(PasswordCredential credential) {
    PBKDF2PasswordCredential pbkdf2Credential = requirePbkdf2Credential(credential);
    PBKDF2HashedPassword hashedPassword = pbkdf2Credential.getHashedPassword();
    String salt = Base64.getEncoder().encodeToString(hashedPassword.getSalt());
    String hash = Base64.getEncoder().encodeToString(hashedPassword.getHash());
    int iterations = hashedPassword.getIterations();
    return "%s$%d$%s$%s".formatted(PBKDF2_WITH_HMAC_SHA256, iterations, salt, hash);
  }

  public List<Credential> mapToDomain(UserEntity userEntity) {
    return Optional.ofNullable(userEntity.getCredentials()).orElse(List.of()).stream()
        .sorted(Comparator.comparing(UserCredentialEntity::getCreated))
        .map(this::decodePassword)
        .map(Credential.class::cast)
        .toList();
  }

  private PasswordCredential decodePassword(UserCredentialEntity userCredentialEntity) {
    var persistedPassword = userCredentialEntity.getValue();
    if (persistedPassword == null || persistedPassword.isBlank()) {
      throw new IllegalStateException("User password is missing");
    }

    String[] parts = persistedPassword.split("\\$", 4);
    if (parts.length != 4) {
      throw new IllegalStateException("Unsupported persisted password format");
    }

    String algorithm = parts[0];
    if (!PBKDF2_WITH_HMAC_SHA256.equals(algorithm)) {
      throw new IllegalStateException("Unsupported password algorithm: " + algorithm);
    }

    int iterations = Integer.parseInt(parts[1]);
    byte[] salt = Base64.getDecoder().decode(parts[2]);
    byte[] hash = Base64.getDecoder().decode(parts[3]);
    PBKDF2HashedPassword hashedPassword = new PBKDF2HashedPassword(hash, salt, iterations);
    return new PBKDF2PasswordCredential(
        userCredentialEntity.getId(), userCredentialEntity.getCreated(), hashedPassword);
  }

  private PBKDF2PasswordCredential requirePbkdf2Credential(PasswordCredential credential) {
    if (!(credential instanceof PBKDF2PasswordCredential pbkdf2PasswordCredential)) {
      throw new IllegalStateException("Unsupported password credential: " + credential.getClass());
    }
    return pbkdf2PasswordCredential;
  }
}
