package pl.michallysak.notes.common;

import lombok.*;
import pl.michallysak.notes.common.exception.ValidationException;

import java.util.regex.Pattern;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Email {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$");
    private final String value;

    public static Email of(String value) {
        if (value == null || !EMAIL_PATTERN.matcher(value).matches()) {
            throw new ValidationException("Invalid email: " + value);
        }
        return new Email(value);
    }
}
