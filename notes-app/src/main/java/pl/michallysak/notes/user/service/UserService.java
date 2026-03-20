package pl.michallysak.notes.user.service;

import pl.michallysak.notes.user.model.CreateUser;
import pl.michallysak.notes.user.model.UserValue;

import java.util.UUID;

public interface UserService {
    UserValue createUser(CreateUser createUser);
    UserValue getUser(UUID userId);
}

