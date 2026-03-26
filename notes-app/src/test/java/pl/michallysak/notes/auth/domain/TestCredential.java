package pl.michallysak.notes.auth.domain;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class TestCredential implements Credential {
  private final OffsetDateTime createdAt;
  private final UUID id;

  public TestCredential() {
    this.id = UUID.randomUUID();
    this.createdAt = OffsetDateTime.now();
  }
}
