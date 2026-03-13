package pl.michallysak.notes.common.validator;

import pl.michallysak.notes.common.exception.ValidationException;

public class CommonValidator {

    public void throwOnNull(Object text, String message) {
        if (text == null) {
            throw new ValidationException(message);
        }
    }

    public void throwOnNotInRange(String text, TextRange range, String message) {
        if (!range.check(text.length())) {
            throw new ValidationException(message);
        }
    }

}
