package pl.michallysak.notes.auth.domain;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.auth.model.HashedPassword;

@Getter
@RequiredArgsConstructor
public abstract class PasswordCredential implements Credential {
  protected final UUID id;
  protected final OffsetDateTime createdAt;

  public abstract HashedPassword getHashedPassword();
}
