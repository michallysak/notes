package pl.michallysak.notes.application.quarkus.note.resource;

import static io.restassured.RestAssured.given;

import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import pl.michallysak.notes.application.quarkus.helpers.RestTestClient;

public class NoteResourceRestTestClient extends RestTestClient {

  private NoteResourceRestTestClient(String token) {
    super("/notes", token);
  }

  public static NoteResourceRestTestClient noAuth() {
    return new NoteResourceRestTestClient(null);
  }

  public static NoteResourceRestTestClient auth(String token) {
    return new NoteResourceRestTestClient(token);
  }

  private String getNotePath(String noteId) {
    return basePath + "/" + noteId;
  }

  public Response createNote(String bodyJson) {
    return given()
        .contentType(MediaType.APPLICATION_JSON)
        .headers(authorizationHeaders)
        .body(bodyJson)
        .when()
        .post(basePath);
  }

  public Response updateNote(String noteId, String bodyJson) {
    return given()
        .contentType(MediaType.APPLICATION_JSON)
        .headers(authorizationHeaders)
        .body(bodyJson)
        .when()
        .put(getNotePath(noteId));
  }

  public Response getNote(String noteId) {
    return given().headers(authorizationHeaders).when().get(getNotePath(noteId));
  }

  public Response getNotes() {
    return given().headers(authorizationHeaders).when().get(basePath);
  }

  public Response deleteNote(String noteId) {
    return given().headers(authorizationHeaders).when().delete(getNotePath(noteId));
  }
}
