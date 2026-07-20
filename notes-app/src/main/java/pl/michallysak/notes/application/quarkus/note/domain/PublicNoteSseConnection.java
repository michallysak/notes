package pl.michallysak.notes.application.quarkus.note.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;

public class PublicNoteSseConnection extends SseConnection {
  @Getter private final UUID publicShareId;

  PublicNoteSseConnection(UUID publicShareId, Set<String> events, Instant expiresAt) {
    super(events, expiresAt);
    this.publicShareId = Objects.requireNonNull(publicShareId);
  }

  public boolean isViewingPublicShare(UUID publicShareId) {
    return this.publicShareId.equals(publicShareId);
  }
}
