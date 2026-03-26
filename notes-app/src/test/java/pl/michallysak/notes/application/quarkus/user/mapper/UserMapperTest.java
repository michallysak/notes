package pl.michallysak.notes.application.quarkus.user.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.quarkus.user.dto.AuthTokenResponse;
import pl.michallysak.notes.application.quarkus.user.dto.LoginUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.RegisterUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.UserResponse;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;

class UserMapperTest {
  UserMapper mapper = new UserMapperImpl();

  @Test
  void mapToEmailPasswordLogin_fromLoginUserRequest() {
    // given
    LoginUserRequest request =
        LoginUserRequest.builder().email("test@example.com").password("Password123!").build();
    // when
    EmailPasswordLogin result = mapper.mapToEmailPasswordLogin(request);
    // then
    assertEquals("test@example.com", result.email().getValue());
    assertEquals("Password123!", result.password().getValue());
  }

  @Test
  void mapToEmailPasswordLogin_fromRegisterUserRequest() {
    // given
    RegisterUserRequest request =
        RegisterUserRequest.builder().email("test@example.com").password("Password123!").build();
    // when
    EmailPasswordLogin result = mapper.mapToEmailPasswordLogin(request);
    // then
    assertEquals("test@example.com", result.email().getValue());
    assertEquals("Password123!", result.password().getValue());
  }

  @Test
  void mapToEmailPasswordCreateUser_fromRegisterUserRequest() {
    // given
    RegisterUserRequest request =
        RegisterUserRequest.builder().email("test@example.com").password("Password123!").build();
    // when
    EmailPasswordCreateUser result = mapper.mapToEmailPasswordCreateUser(request);
    // then
    assertEquals("test@example.com", result.email().getValue());
    assertEquals("Password123!", result.password().getValue());
  }

  @Test
  void mapToUserResponse_shouldMapFields() {
    // given
    UserValue userValue =
        UserValue.builder()
            .id(java.util.UUID.randomUUID())
            .email(Email.of("test@example.com"))
            .build();
    // when
    UserResponse response = mapper.mapToUserResponse(userValue);
    // then
    assertEquals("test@example.com", response.getEmail());
  }

  @Test
  void mapToAuthTokenResponse_shouldMapFields() {
    // given
    AuthToken token = new AuthToken("token", OffsetDateTime.now().plusHours(1));
    // when
    AuthTokenResponse response = mapper.mapToAuthTokenResponse(token);
    // then
    assertEquals("token", response.getToken());
  }

  @Test
  void map_shouldReturnNullForNullEmail() {
    // given
    Email email = null;
    // when
    String result = mapper.map(email);
    // then
    assertNull(result);
  }

  @Test
  void mapToEmailPasswordLogin_fromLoginUserRequest_nullInput() {
    // given
    LoginUserRequest request = null;
    // when
    EmailPasswordLogin result = mapper.mapToEmailPasswordLogin(request);
    // then
    assertNull(result);
  }

  @Test
  void mapToEmailPasswordLogin_fromRegisterUserRequest_nullInput() {
    // given
    RegisterUserRequest request = null;
    // when
    EmailPasswordLogin result = mapper.mapToEmailPasswordLogin(request);
    // then
    assertNull(result);
  }

  @Test
  void mapToEmailPasswordCreateUser_fromRegisterUserRequest_nullInput() {
    // given
    RegisterUserRequest request = null;
    // when
    EmailPasswordCreateUser result = mapper.mapToEmailPasswordCreateUser(request);
    // then
    assertNull(result);
  }

  @Test
  void mapToUserResponse_nullInput() {
    // given
    UserValue userValue = null;
    // when
    UserResponse result = mapper.mapToUserResponse(userValue);
    // then
    assertNull(result);
  }

  @Test
  void mapToAuthTokenResponse_nullInput() {
    // given
    AuthToken token = null;
    // when
    AuthTokenResponse result = mapper.mapToAuthTokenResponse(token);
    // then
    assertNull(result);
  }
}
