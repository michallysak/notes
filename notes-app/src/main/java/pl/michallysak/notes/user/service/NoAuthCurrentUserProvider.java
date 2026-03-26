package pl.michallysak.notes.user.service;

import java.util.UUID;
import lombok.Getter;

@Getter
public class NoAuthCurrentUserProvider implements CurrentUserProvider {
  private final UUID currentUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Override
  public UUID getCurrentUserId() {
    return currentUserId;
  }
}
