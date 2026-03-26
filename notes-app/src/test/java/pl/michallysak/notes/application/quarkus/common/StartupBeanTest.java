package pl.michallysak.notes.application.quarkus.common;

import static org.mockito.Mockito.*;

import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.auth.model.Password;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class StartupBeanTest {
  @Mock Logger logger;
  @Mock UserService userService;
  @InjectMocks StartupBean startupBean;

  @Test
  void onStart_shouldCreateAndLoginUser_andLogInfo() {
    // given
    Email email = Email.of("admin@test.pl");
    Password password = Password.of("Admin123!");
    when(userService.createUser(any(EmailPasswordCreateUser.class)))
        .thenReturn(mock(UserValue.class));
    when(userService.login(any(EmailPasswordLogin.class))).thenReturn(mock(AuthToken.class));
    // when
    startupBean.onStart(mock(StartupEvent.class));
    // then
    verify(userService)
        .createUser(argThat(arg -> arg.email().equals(email) && arg.password().equals(password)));
    verify(userService)
        .login(argThat(arg -> arg.email().equals(email) && arg.password().equals(password)));
    verify(logger).info(contains("Created default user:"));
    verify(logger).info(contains("Login Successful:"));
  }
}
