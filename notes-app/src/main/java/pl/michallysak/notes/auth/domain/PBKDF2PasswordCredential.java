package pl.michallysak.notes.auth.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import pl.michallysak.notes.auth.model.PBKDF2HashedPassword;

@Getter
public final class PBKDF2PasswordCredential extends PasswordCredential {
  private final PBKDF2HashedPassword hashedPassword;

  public PBKDF2PasswordCredential(PBKDF2HashedPassword hashedPassword) {
    super(UUID.randomUUID(), OffsetDateTime.now());
    this.hashedPassword = Objects.requireNonNull(hashedPassword);
  }

  public PBKDF2PasswordCredential(
      UUID id, OffsetDateTime createdAt, PBKDF2HashedPassword hashedPassword) {
    super(Objects.requireNonNull(id), Objects.requireNonNull(createdAt));
    this.hashedPassword = Objects.requireNonNull(hashedPassword);
  }

  @Override
  public PBKDF2HashedPassword getHashedPassword() {
    return hashedPassword;
  }
}
