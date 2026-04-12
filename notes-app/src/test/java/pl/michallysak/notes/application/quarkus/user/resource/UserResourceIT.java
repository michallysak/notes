package pl.michallysak.notes.application.quarkus.user.resource;

import static org.junit.jupiter.api.Assertions.*;
import static pl.michallysak.notes.application.quarkus.user.dto.UserDtoRequestUtils.*;
import static pl.michallysak.notes.helpers.TestExtensions.toJsonString;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.quarkus.helpers.BaseIT;
import pl.michallysak.notes.application.quarkus.user.dto.LoginUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.RegisterUserRequest;

@QuarkusTest
class UserResourceIT extends BaseIT {

  @Test
  void registerUser_shouldReturn201AndToken() {
    // given
    UserResourceRestTestClient userClient = UserResourceRestTestClient.noAuth();
    RegisterUserRequest request = getRegisterUserRequestBuilder(EMAIL_1).build();
    // when
    Response response = userClient.registerUser(toJsonString(request));
    // then
    response.then().statusCode(201);
    assertNotNull(response.jsonPath().getString("token"));
  }

  @Test
  void registerUser_shouldReturn400_whenInvalidRequest() {
    // given
    UserResourceRestTestClient userClient = UserResourceRestTestClient.noAuth();
    RegisterUserRequest request = getRegisterUserRequestBuilder(EMAIL_1).email("").build();
    // when
    Response response = userClient.registerUser(toJsonString(request));
    // then
    response.then().statusCode(400);
  }

  @Test
  void loginUser_shouldReturn201_whenValidCredentials() {
    // given
    UserResourceRestTestClient userClient = UserResourceRestTestClient.noAuth();
    RegisterUserRequest registerRequest = getRegisterUserRequestBuilder(EMAIL_1).build();
    userClient.registerUser(toJsonString(registerRequest)).then().statusCode(201);
    LoginUserRequest loginRequest =
        getLoginUserRequestBuilder(EMAIL_1).password(registerRequest.getPassword()).build();
    // when
    Response response = userClient.loginUser(toJsonString(loginRequest));
    // then
    response.then().statusCode(201);
    assertNotNull(response.jsonPath().getString("token"));
  }

  @Test
  void loginUser_shouldReturn400_whenInvalidRequest() {
    // given
    UserResourceRestTestClient userClient = UserResourceRestTestClient.noAuth();
    LoginUserRequest request = getLoginUserRequestBuilder(EMAIL_1).email("").build();
    // when
    Response response = userClient.loginUser(toJsonString(request));
    // then
    response.then().statusCode(400);
  }

  @Test
  void loginUser_shouldReturn401_whenInvalidCredentials() {
    // given
    createUser(EMAIL_1);
    UserResourceRestTestClient userClient = UserResourceRestTestClient.noAuth();
    LoginUserRequest request = getLoginUserRequestBuilder(EMAIL_1).password("WrongPass!").build();
    // when
    Response response = userClient.loginUser(toJsonString(request));
    // then
    response.then().statusCode(401);
  }

  @Test
  void me_shouldReturn200_whenAuthenticated() {
    // given
    String token = createUser(EMAIL_1);
    UserResourceRestTestClient authClient = UserResourceRestTestClient.auth(token);
    // when
    Response response = authClient.me();
    // then
    response.then().statusCode(200);
    assertEquals(EMAIL_1, response.jsonPath().getString("email"));
    assertDoesNotThrow(
        () -> {
          String id = response.jsonPath().getString("id");
          UUID.fromString(id);
        });
  }

  @Test
  void me_shouldReturn401_whenNoAuth() {
    // given
    UserResourceRestTestClient userClient = UserResourceRestTestClient.noAuth();
    // when
    Response response = userClient.me();
    // then
    response.then().statusCode(401);
  }
}
