package pl.michallysak.notes.common.validator;

import java.util.regex.Pattern;
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

  public void throwOnNotMatch(String text, Pattern pattern, String message) {
    if (!pattern.matcher(text).matches()) {
      throw new ValidationException(message);
    }
  }
}
