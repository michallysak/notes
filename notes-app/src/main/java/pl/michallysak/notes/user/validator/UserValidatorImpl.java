package pl.michallysak.notes.user.validator;

import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.common.validator.CommonValidator;
import pl.michallysak.notes.common.validator.TextRange;
import pl.michallysak.notes.user.model.CreateUser;

public class UserValidatorImpl implements UserValidator {
    private static final TextRange EMAIL_LENGTH_RANGE = TextRange.of(7, 32);
    private final CommonValidator commonValidator = new CommonValidator();

    @Override
    public void validateCreateUser(CreateUser createUser) throws ValidationException {
        commonValidator.throwOnNull(createUser, "CreateUser cannot be null");
        Email email = createUser.email();
        commonValidator.throwOnNull(email, "Email cannot be null");
        commonValidator.throwOnNotInRange(email.getValue(), EMAIL_LENGTH_RANGE, "Email must be between %s characters, is %d".formatted(EMAIL_LENGTH_RANGE, email.getValue().length()));
    }
}
