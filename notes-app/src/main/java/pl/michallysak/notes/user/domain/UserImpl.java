package pl.michallysak.notes.user.domain;

import lombok.Getter;
import lombok.ToString;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.model.CreateUser;
import pl.michallysak.notes.user.validator.UserValidator;

import java.util.Objects;
import java.util.UUID;

@Getter
@ToString
public class UserImpl implements User {
    private final UUID id;
    private final Email email;
    private final String password;

    private final UserValidator userValidator;

    public UserImpl(CreateUser createUser, UserValidator userValidator) {
        this.userValidator = Objects.requireNonNull(userValidator);
        userValidator.validateCreateUser(createUser);
        this.id = UUID.randomUUID();
        this.email = createUser.email();
        this.password = createUser.password();
    }
}
