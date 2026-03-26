package pl.michallysak.notes.user.model;

import java.util.UUID;
import lombok.Builder;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.domain.User;

@Builder
public record UserValue(UUID id, Email email) {
  public static UserValue from(User user) {
    return UserValue.builder().id(user.getId()).email(user.getEmail()).build();
  }
}
