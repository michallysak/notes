package pl.michallysak.notes.user.service;

import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;

import java.util.UUID;

public interface UserService {
    UserValue createUser(EmailPasswordCreateUser createUser);

    AuthToken login(EmailPasswordLogin login);

    UserValue getUser(UUID userId);
}

