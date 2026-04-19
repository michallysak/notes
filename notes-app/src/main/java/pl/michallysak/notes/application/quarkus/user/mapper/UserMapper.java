package pl.michallysak.notes.application.quarkus.user.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.michallysak.notes.application.quarkus.user.dto.AuthTokenResponse;
import pl.michallysak.notes.application.quarkus.user.dto.LoginUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.RegisterUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.UserResponse;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.domain.User;
import pl.michallysak.notes.user.domain.UserImpl;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.repository.UserCredentialEntity;
import pl.michallysak.notes.user.repository.UserEntity;
import pl.michallysak.notes.user.validator.UserValidator;

/**
 * For abstract classes or decorators setter injection should be used.
 *
 * @see <a href="https://mapstruct.org/documentation/stable/reference/html/#injection-strategy">
 *     MapStruct Injection Strategy</a>
 */
@Mapper(componentModel = "cdi")
@ApplicationScoped
public abstract class UserMapper {
  protected UserValidator userValidator;
  protected CredentialMapper credentialMapper;

  @Inject
  public void setUserValidator(UserValidator userValidator) {
    this.userValidator = userValidator;
  }

  @Inject
  public void setCredentialMapper(CredentialMapper credentialMapper) {
    this.credentialMapper = credentialMapper;
  }

  @Mapping(
      target = "email",
      expression = "java(pl.michallysak.notes.common.Email.of(request.getEmail()))")
  @Mapping(
      target = "password",
      expression = "java(pl.michallysak.notes.auth.model.Password.of(request.getPassword()))")
  public abstract EmailPasswordLogin mapToEmailPasswordLogin(LoginUserRequest request);

  @Mapping(
      target = "email",
      expression = "java(pl.michallysak.notes.common.Email.of(request.getEmail()))")
  @Mapping(
      target = "password",
      expression = "java(pl.michallysak.notes.auth.model.Password.of(request.getPassword()))")
  public abstract EmailPasswordLogin mapToEmailPasswordLogin(RegisterUserRequest request);

  @Mapping(
      target = "email",
      expression = "java(pl.michallysak.notes.common.Email.of(request.getEmail()))")
  @Mapping(
      target = "password",
      expression = "java(pl.michallysak.notes.auth.model.Password.of(request.getPassword()))")
  public abstract EmailPasswordCreateUser mapToEmailPasswordCreateUser(RegisterUserRequest request);

  public abstract UserResponse mapToUserResponse(UserValue userValue);

  public abstract AuthTokenResponse mapToAuthTokenResponse(AuthToken authToken);

  public String map(Email value) {
    return Optional.ofNullable(value).map(Email::getValue).orElse(null);
  }

  public UserEntity mapToEntity(User user) {
    List<UserCredentialEntity> credentials = credentialMapper.mapToEntities(user);
    UserEntity userEntity = new UserEntity();
    userEntity.setId(user.getId());
    userEntity.setEmail(user.getEmail().getValue());
    userEntity.setCredentials(credentials);
    userEntity.setCreated(getCreated(credentials));
    return userEntity;
  }

  public User mapToDomain(UserEntity userEntity) {
    return new UserImpl(
        mapToUserValue(userEntity), credentialMapper.mapToDomain(userEntity), userValidator);
  }

  private UserValue mapToUserValue(UserEntity userEntity) {
    return UserValue.builder()
        .id(userEntity.getId())
        .email(Email.of(userEntity.getEmail()))
        .build();
  }

  private OffsetDateTime getCreated(List<UserCredentialEntity> credentials) {
    return credentials.stream()
        .map(UserCredentialEntity::getCreated)
        .min(Comparator.naturalOrder())
        .orElseThrow(() -> new IllegalStateException("User has no password credential"));
  }
}
