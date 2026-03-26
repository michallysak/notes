package pl.michallysak.notes.application.quarkus.user.resource;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.user.controller.UserController;
import pl.michallysak.notes.application.quarkus.user.dto.AuthTokenResponse;
import pl.michallysak.notes.application.quarkus.user.dto.LoginUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.RegisterUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.UserResponse;

@ExtendWith(MockitoExtension.class)
class UserResourceTest {
  @Mock UserController userController;
  @InjectMocks UserResource userResource;

  @Test
  void register_shouldDelegateToController() {
    // given
    RegisterUserRequest request = mock(RegisterUserRequest.class);
    AuthTokenResponse response = mock(AuthTokenResponse.class);
    when(userController.register(request)).thenReturn(response);
    // when
    userResource.register(request);
    // then
    verify(userController).register(request);
  }

  @Test
  void login_shouldDelegateToController() {
    // given
    LoginUserRequest request = mock(LoginUserRequest.class);
    AuthTokenResponse response = mock(AuthTokenResponse.class);
    when(userController.login(request)).thenReturn(response);
    // when
    userResource.login(request);
    // then
    verify(userController).login(request);
  }

  @Test
  void me_shouldDelegateToController() {
    // given
    UserResponse response = mock(UserResponse.class);
    when(userController.me()).thenReturn(response);
    // when
    userResource.me();
    // then
    verify(userController).me();
  }
}
