package pl.michallysak.notes.user;

import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.model.CreateUser;
import pl.michallysak.notes.user.model.UserValue;

import java.util.UUID;

public class UserTestUtils {
    private static final String DEFAULT_EMAIL = "test@example.com";
    private static final UUID DEFAULT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String PASSWORD = "password";

    public static CreateUser.CreateUserBuilder createCreateUserBuilder() {
        return CreateUser.builder().email(Email.of(DEFAULT_EMAIL)).password(PASSWORD);
    }

    public static UserValue.UserValueBuilder createUserValueBuilder() {
        return UserValue.builder().id(DEFAULT_ID).email(Email.of(DEFAULT_EMAIL));
    }
}

