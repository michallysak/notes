package pl.michallysak.notes.user.service;

import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.user.domain.User;
import pl.michallysak.notes.user.domain.UserImpl;
import pl.michallysak.notes.user.model.CreateUser;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.repository.UserRepository;
import pl.michallysak.notes.user.exception.UserNotFoundException;
import pl.michallysak.notes.user.validator.UserValidator;

import java.util.UUID;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    @Override
    public UserValue createUser(CreateUser createUser) {
        User user = new UserImpl(createUser, userValidator);
        userRepository.save(user);
        return UserValue.from(user);
    }

    @Override
    public UserValue getUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        return UserValue.from(user);
    }
}

