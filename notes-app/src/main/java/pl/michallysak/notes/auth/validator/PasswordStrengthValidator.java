package pl.michallysak.notes.auth.validator;

import pl.michallysak.notes.auth.model.Password;

public interface PasswordStrengthValidator {
  void validatePassword(Password password);
}
