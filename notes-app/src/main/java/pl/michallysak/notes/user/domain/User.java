package pl.michallysak.notes.user.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pl.michallysak.notes.auth.domain.Credential;
import pl.michallysak.notes.common.Email;

public interface User {
  UUID getId();

  Email getEmail();

  void addCredential(Credential credential);

  <T extends Credential> List<T> getCredentials(Class<T> type);

  <T extends Credential> void deleteCredentials(Class<T> type);

  <T extends Credential> Optional<T> getLatestCredential(Class<T> type);
}
