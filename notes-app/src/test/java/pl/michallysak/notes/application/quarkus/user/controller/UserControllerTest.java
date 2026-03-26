package pl.michallysak.notes.application.quarkus.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.user.dto.AuthTokenResponse;
import pl.michallysak.notes.application.quarkus.user.dto.LoginUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.RegisterUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.UserResponse;
import pl.michallysak.notes.application.quarkus.user.mapper.UserMapper;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.service.CurrentUserProvider;
import pl.michallysak.notes.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
  @Mock UserService userService;
  @Mock CurrentUserProvider currentUserProvider;
  @Mock UserMapper userMapper;
  @InjectMocks UserController userController;

  @Test
  void register_shouldMapAndDelegate() {
    // given
    RegisterUserRequest request = mock(RegisterUserRequest.class);
    EmailPasswordCreateUser createUser = mock(EmailPasswordCreateUser.class);
    EmailPasswordLogin login = mock(EmailPasswordLogin.class);
    AuthTokenResponse response = mock(AuthTokenResponse.class);
    when(userMapper.mapToEmailPasswordCreateUser(request)).thenReturn(createUser);
    when(userMapper.mapToEmailPasswordLogin(request)).thenReturn(login);
    when(userService.createUser(createUser)).thenReturn(mock(UserValue.class));
    when(userService.login(login)).thenReturn(mock(AuthToken.class));
    when(userMapper.mapToAuthTokenResponse(any())).thenReturn(response);
    // when
    AuthTokenResponse result = userController.register(request);
    // then
    assertEquals(response, result);
    verify(userMapper).mapToEmailPasswordCreateUser(request);
    verify(userService).createUser(createUser);
    verify(userMapper).mapToEmailPasswordLogin(request);
    verify(userService).login(login);
    verify(userMapper).mapToAuthTokenResponse(any());
  }

  @Test
  void login_shouldMapAndDelegate() {
    // given
    LoginUserRequest request = mock(LoginUserRequest.class);
    EmailPasswordLogin login = mock(EmailPasswordLogin.class);
    AuthTokenResponse response = mock(AuthTokenResponse.class);
    when(userMapper.mapToEmailPasswordLogin(request)).thenReturn(login);
    when(userService.login(login)).thenReturn(mock(AuthToken.class));
    when(userMapper.mapToAuthTokenResponse(any())).thenReturn(response);
    // when
    AuthTokenResponse result = userController.login(request);
    // then
    assertEquals(response, result);
    verify(userMapper).mapToEmailPasswordLogin(request);
    verify(userService).login(login);
    verify(userMapper).mapToAuthTokenResponse(any());
  }

  @Test
  void me_shouldReturnMappedUser() {
    // given
    UUID userId = UUID.randomUUID();
    UserValue userValue = mock(UserValue.class);
    UserResponse response = mock(UserResponse.class);
    when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
    when(userService.getUser(userId)).thenReturn(userValue);
    when(userMapper.mapToUserResponse(userValue)).thenReturn(response);
    // when
    UserResponse result = userController.me();
    // then
    assertEquals(response, result);
    verify(currentUserProvider).getCurrentUserId();
    verify(userService).getUser(userId);
    verify(userMapper).mapToUserResponse(userValue);
  }
}
