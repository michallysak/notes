package pl.michallysak.notes.user.validator;

import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.user.model.CreateUser;

public interface UserValidator {
    void validateCreateUser(CreateUser createUser) throws ValidationException;
}

