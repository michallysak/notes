package pl.michallysak.notes.user.validator;

import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.auth.validator.PasswordStrengthValidator;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.common.validator.CommonValidator;
import pl.michallysak.notes.common.validator.TextRange;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;

@RequiredArgsConstructor
public class UserValidatorImpl implements UserValidator {
  private static final TextRange EMAIL_LENGTH_RANGE = TextRange.of(7, 32);
  private final CommonValidator commonValidator = new CommonValidator();
  private final PasswordStrengthValidator passwordStrengthValidator;

  @Override
  public void validateCreateUser(EmailPasswordCreateUser createUser) throws ValidationException {
    commonValidator.throwOnNull(createUser, "CreateUser cannot be null");
    Email email = createUser.email();
    commonValidator.throwOnNull(email, "Email cannot be null");
    commonValidator.throwOnNotInRange(
        email.getValue(),
        EMAIL_LENGTH_RANGE,
        "Email must be between %s characters, is %d"
            .formatted(EMAIL_LENGTH_RANGE, email.getValue().length()));
    passwordStrengthValidator.validatePassword(createUser.password());
  }
}
