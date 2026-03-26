package pl.michallysak.notes.user.repository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.domain.User;

public class InMemoryUserRepository implements UserRepository {
  private final Map<UUID, User> users;

  public InMemoryUserRepository() {
    users = new HashMap<>();
  }

  public InMemoryUserRepository(List<User> initialUsers) {
    this.users = initialUsers.stream().collect(Collectors.toMap(User::getId, Function.identity()));
  }

  @Override
  public void save(User user) {
    if (!users.containsKey(user.getId())) {
      users.put(user.getId(), user);
    }
  }

  @Override
  public Optional<User> findById(UUID id) {
    return Optional.ofNullable(users.get(id));
  }

  @Override
  public boolean existsByEmail(Email email) {
    return users.values().stream().anyMatch(user -> user.getEmail().equals(email));
  }

  @Override
  public Optional<User> findByEmail(Email email) {
    return users.values().stream().filter(user -> user.getEmail().equals(email)).findFirst();
  }
}
