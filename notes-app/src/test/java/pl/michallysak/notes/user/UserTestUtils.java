package pl.michallysak.notes.user;

import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.model.CreateUser;
import pl.michallysak.notes.user.model.UserValue;

public class UserTestUtils {
    private static final String DEFAULT_EMAIL = "test@example.com";
    private static final String PASSWORD = "password";

    public static CreateUser.CreateUserBuilder createCreateUserBuilder() {
        return CreateUser.builder().email(Email.of(DEFAULT_EMAIL)).password(PASSWORD);
    }

}

