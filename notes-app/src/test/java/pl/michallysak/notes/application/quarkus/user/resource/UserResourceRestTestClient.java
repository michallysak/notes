package pl.michallysak.notes.application.quarkus.user.resource;

import static io.restassured.RestAssured.given;

import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import pl.michallysak.notes.application.quarkus.helpers.RestTestClient;

public class UserResourceRestTestClient extends RestTestClient {

  private UserResourceRestTestClient(String token) {
    super("/users", token);
  }

  public static UserResourceRestTestClient noAuth() {
    return new UserResourceRestTestClient(null);
  }

  public static UserResourceRestTestClient auth(String token) {
    return new UserResourceRestTestClient(token);
  }

  public Response registerUser(String bodyJson) {
    return given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(bodyJson)
        .when()
        .post(basePath + "/register");
  }

  public Response loginUser(String bodyJson) {
    return given()
        .contentType(MediaType.APPLICATION_JSON)
        .body(bodyJson)
        .when()
        .post(basePath + "/login");
  }

  public Response me() {
    return given().headers(authorizationHeaders).when().get(basePath + "/me");
  }
}
