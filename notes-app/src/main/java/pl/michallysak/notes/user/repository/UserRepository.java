package pl.michallysak.notes.user.repository;

import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    void save(User user);

    Optional<User> findById(UUID id);

    boolean existsByEmail(Email email);

    Optional<User> findByEmail(Email email);
}

