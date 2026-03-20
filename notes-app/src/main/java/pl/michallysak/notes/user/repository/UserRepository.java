package pl.michallysak.notes.user.repository;

import pl.michallysak.notes.user.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    void save(User user);
    Optional<User> findById(UUID id);
}

