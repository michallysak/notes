package pl.michallysak.notes.auth.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface Credential {
  UUID getId();

  OffsetDateTime getCreatedAt();
}
