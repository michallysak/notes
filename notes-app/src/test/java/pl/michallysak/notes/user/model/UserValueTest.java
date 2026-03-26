package pl.michallysak.notes.user.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.user.UserTestUtils;
import pl.michallysak.notes.user.domain.User;
import pl.michallysak.notes.user.domain.UserImpl;
import pl.michallysak.notes.user.validator.UserValidator;

@ExtendWith(MockitoExtension.class)
class UserValueTest {

  @Mock private UserValidator userValidator;

  @Test
  void from_shouldMapUserFieldsCorrectly() {
    // given
    EmailPasswordCreateUser emailPasswordCreateUser =
        UserTestUtils.createEmailPasswordCreateUserBuilder().build();
    User user = new UserImpl(emailPasswordCreateUser, userValidator);
    // when
    UserValue value = UserValue.from(user);
    // then
    assertEquals(user.getId(), value.id());
    assertEquals(user.getEmail(), value.email());
  }
}
