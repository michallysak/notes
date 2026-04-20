package pl.michallysak.notes.application.quarkus.user.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import pl.michallysak.notes.application.quarkus.user.dto.AuthTokenResponse;
import pl.michallysak.notes.application.quarkus.user.dto.LoginUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.RegisterUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.UserResponse;
import pl.michallysak.notes.auth.domain.Credential;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.auth.model.Password;
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
@Setter(onMethod_ = @Inject)
@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
@ApplicationScoped
public abstract class UserMapper {
  protected UserValidator userValidator;
  protected CredentialMapper credentialMapper;

  public abstract EmailPasswordLogin mapToEmailPasswordLogin(LoginUserRequest request);

  public abstract EmailPasswordLogin mapToEmailPasswordLogin(RegisterUserRequest request);

  public abstract EmailPasswordCreateUser mapToEmailPasswordCreateUser(RegisterUserRequest request);

  public abstract UserResponse mapToUserResponse(UserValue userValue);

  public abstract AuthTokenResponse mapToAuthTokenResponse(AuthToken authToken);

  protected String mapToEmailValue(Email value) {
    return Optional.ofNullable(value).map(Email::getValue).orElse(null);
  }

  protected Email mapToEmail(String value) {
    return Email.of(value);
  }

  protected Password mapToPassword(String value) {
    return Password.of(value);
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
    UserValue userValue = mapToUserValue(userEntity);
    List<Credential> credentials = credentialMapper.mapToDomain(userEntity);
    return new UserImpl(userValue, credentials, userValidator);
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
