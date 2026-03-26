package pl.michallysak.notes.application.quarkus.user.mapper;

import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.michallysak.notes.application.quarkus.user.dto.AuthTokenResponse;
import pl.michallysak.notes.application.quarkus.user.dto.LoginUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.RegisterUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.UserResponse;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;

@Mapper(componentModel = "cdi")
public interface UserMapper {
  @Mapping(
      target = "email",
      expression = "java(pl.michallysak.notes.common.Email.of(request.getEmail()))")
  @Mapping(
      target = "password",
      expression = "java(pl.michallysak.notes.auth.model.Password.of(request.getPassword()))")
  EmailPasswordLogin mapToEmailPasswordLogin(LoginUserRequest request);

  @Mapping(
      target = "email",
      expression = "java(pl.michallysak.notes.common.Email.of(request.getEmail()))")
  @Mapping(
      target = "password",
      expression = "java(pl.michallysak.notes.auth.model.Password.of(request.getPassword()))")
  EmailPasswordLogin mapToEmailPasswordLogin(RegisterUserRequest request);

  @Mapping(
      target = "email",
      expression = "java(pl.michallysak.notes.common.Email.of(request.getEmail()))")
  @Mapping(
      target = "password",
      expression = "java(pl.michallysak.notes.auth.model.Password.of(request.getPassword()))")
  EmailPasswordCreateUser mapToEmailPasswordCreateUser(RegisterUserRequest request);

  UserResponse mapToUserResponse(UserValue userValue);

  AuthTokenResponse mapToAuthTokenResponse(AuthToken authToken);

  default String map(Email value) {
    return Optional.ofNullable(value).map(Email::getValue).orElse(null);
  }
}
