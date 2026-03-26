package pl.michallysak.notes.user.validator;

import java.util.stream.Stream;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.helpers.TestExtensions;
import pl.michallysak.notes.user.UserTestUtils;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;

class UserValidatorImplTestUtils {
  private static final int EMAIL_MIN_LENGTH = 7;
  private static final int EMAIL_MAX_LENGTH = 32;
  private static final String EMAIL_BASE = "@b.pl";

  public static Stream<EmailPasswordCreateUser> createUsersWithNotInRangeLengthEmail() {
    return Stream.of(EMAIL_MIN_LENGTH - 1, EMAIL_MAX_LENGTH + 1)
        .map(length -> TestExtensions.textsWithLength(length, 'a'))
        .flatMap(UserValidatorImplTestUtils::mapTextLength);
  }

  private static Stream<EmailPasswordCreateUser> mapTextLength(Stream<String> emailValues) {
    return emailValues.map(
        emailValue -> {
          String emailValueSubstring =
              emailValue.substring(0, emailValue.length() - EMAIL_BASE.length());
          return UserTestUtils.createEmailPasswordCreateUserBuilder()
              .email(Email.of(emailValueSubstring + EMAIL_BASE))
              .build();
        });
  }
}
