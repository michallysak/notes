package pl.michallysak.notes.user.repository;

import pl.michallysak.notes.user.domain.User;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InMemoryUserRepository implements UserRepository {
    private final Map<UUID, User> users;

    public InMemoryUserRepository() {
        users = new HashMap<>();
    }

    public InMemoryUserRepository(List<User> initialUsers) {
        this.users = initialUsers.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    @Override
    public void save(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }

}

