package pl.michallysak.notes.auth.validator;

import pl.michallysak.notes.auth.model.Password;
import pl.michallysak.notes.common.validator.CommonValidator;
import pl.michallysak.notes.common.validator.TextRange;

import java.util.regex.Pattern;

public class PasswordStrengthValidatorImpl implements PasswordStrengthValidator {
    private static final TextRange PASSWORD_RANGE = TextRange.of(8, 64);

    private static final String MESSAGE = "Password must be between %d and %d characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character.".formatted(PASSWORD_RANGE.getMin(), PASSWORD_RANGE.getMax());

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile(".*[^A-Za-z0-9].*");

    private final CommonValidator commonValidator = new CommonValidator();

    @Override
    public void validatePassword(Password password) {
        commonValidator.throwOnNull(password, "Password cannot be null.");
        String value = password.getValue();
        commonValidator.throwOnNull(value, "Password cannot be null.");
        commonValidator.throwOnNotInRange(value, PASSWORD_RANGE, MESSAGE);
        commonValidator.throwOnNotMatch(value, UPPERCASE_PATTERN, MESSAGE);
        commonValidator.throwOnNotMatch(value, LOWERCASE_PATTERN, MESSAGE);
        commonValidator.throwOnNotMatch(value, DIGIT_PATTERN, MESSAGE);
        commonValidator.throwOnNotMatch(value, SPECIAL_PATTERN, MESSAGE);
    }
}
