package pl.michallysak.notes.user.validator;

import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;

public interface UserValidator {
    void validateCreateUser(EmailPasswordCreateUser createUser) throws ValidationException;
}

