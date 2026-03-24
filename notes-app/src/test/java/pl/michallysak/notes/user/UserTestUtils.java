package pl.michallysak.notes.user;

import pl.michallysak.notes.auth.model.Password;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;

public class UserTestUtils {
    private static final Email DEFAULT_EMAIL = Email.of("test@example.com");
    private static final Password PASSWORD = Password.of("Password123!");

    public static EmailPasswordCreateUser.EmailPasswordCreateUserBuilder createEmailPasswordCreateUserBuilder() {
        return EmailPasswordCreateUser.builder()
                .email(DEFAULT_EMAIL)
                .password(PASSWORD);
    }

}
