package pl.michallysak.notes.user.exception;

import pl.michallysak.notes.common.exception.EntityNotFoundException;

public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException() {
        super("User not found");
    }
}

