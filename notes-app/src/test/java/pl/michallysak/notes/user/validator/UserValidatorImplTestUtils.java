package pl.michallysak.notes.user.validator;

import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.helpers.TestExtensions;
import pl.michallysak.notes.user.UserTestUtils;
import pl.michallysak.notes.user.model.CreateUser;

import java.util.stream.Stream;

class UserValidatorImplTestUtils {
    private static final int EMAIL_MIN_LENGTH = 7;
    private static final int EMAIL_MAX_LENGTH = 32;
    private static final String EMAIL_BASE = "@b.pl";

    public static Stream<CreateUser> createUsersWithNotInRangeLengthEmail() {
        return Stream.of(EMAIL_MIN_LENGTH - 1, EMAIL_MAX_LENGTH + 1)
                .map(length -> TestExtensions.textsWithLength(length, 'a'))
                .flatMap(UserValidatorImplTestUtils::getBuild);
    }

    private static Stream<CreateUser> getBuild(Stream<String> local) {
        return local.map(a -> {
            String substring = a.substring(0, a.length() - EMAIL_BASE.length());
            return UserTestUtils.createCreateUserBuilder().email(Email.of(substring + EMAIL_BASE)).build();
        });
    }

}
