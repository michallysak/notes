package pl.michallysak.notes.user.repository;

import java.util.Optional;
import java.util.UUID;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.domain.User;

public interface UserRepository {
  void saveUser(User user);

  Optional<User> findUserWithId(UUID id);

  boolean existsWithEmail(Email email);

  Optional<User> findUserWithEmail(Email email);

  void deleteUsers();
}
