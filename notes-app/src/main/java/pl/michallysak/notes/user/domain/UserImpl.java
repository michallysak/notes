package pl.michallysak.notes.user.domain;

import java.util.*;
import lombok.Getter;
import lombok.ToString;
import pl.michallysak.notes.auth.domain.Credential;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.validator.UserValidator;

@Getter
@ToString
public class UserImpl implements User {
  private final UUID id;
  private final Email email;
  private final List<Credential> credentials = Collections.synchronizedList(new ArrayList<>());
  private final UserValidator userValidator;

  public UserImpl(EmailPasswordCreateUser emailPasswordCreateUser, UserValidator userValidator) {
    this.userValidator = Objects.requireNonNull(userValidator);
    userValidator.validateCreateUser(emailPasswordCreateUser);
    this.id = UUID.randomUUID();
    this.email = emailPasswordCreateUser.email();
  }

  @Override
  public <T extends Credential> List<T> getCredentials(Class<T> type) {
    return credentials.stream().filter(type::isInstance).map(type::cast).toList();
  }

  @Override
  public <T extends Credential> void deleteCredentials(Class<T> type) {
    credentials.removeIf(type::isInstance);
  }

  @Override
  public <T extends Credential> Optional<T> getLatestCredential(Class<T> type) {
    return credentials.stream()
        .filter(type::isInstance)
        .map(type::cast)
        .max(Comparator.comparing(Credential::getCreatedAt));
  }

  @Override
  public void addCredential(Credential credential) {
    credentials.add(Objects.requireNonNull(credential));
  }
}
