package pl.michallysak.notes.application.quarkus.user.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.application.quarkus.user.dto.AuthTokenResponse;
import pl.michallysak.notes.application.quarkus.user.dto.LoginUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.RegisterUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.UserResponse;
import pl.michallysak.notes.application.quarkus.user.mapper.UserMapper;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.service.CurrentUserProvider;
import pl.michallysak.notes.user.service.UserService;

@ApplicationScoped
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final CurrentUserProvider currentUserProvider;
  private final UserMapper userMapper;

  @Transactional
  public AuthTokenResponse register(RegisterUserRequest request) {
    EmailPasswordCreateUser createUser = userMapper.mapToEmailPasswordCreateUser(request);
    userService.createUser(createUser);
    EmailPasswordLogin login = userMapper.mapToEmailPasswordLogin(request);
    return userMapper.mapToAuthTokenResponse(userService.login(login));
  }

  public AuthTokenResponse login(LoginUserRequest request) {
    EmailPasswordLogin login = userMapper.mapToEmailPasswordLogin(request);
    return userMapper.mapToAuthTokenResponse(userService.login(login));
  }

  public UserResponse me() {
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    UserValue user = userService.getUser(currentUserId);
    return userMapper.mapToUserResponse(user);
  }
}
