package pl.michallysak.notes.application.quarkus.note.attachment.resource;

import static io.restassured.RestAssured.given;

import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import pl.michallysak.notes.application.quarkus.helpers.RestTestClient;

public class NoteAttachmentResourceRestTestClient extends RestTestClient {

  private NoteAttachmentResourceRestTestClient(String token) {
    super("/attachments", token);
  }

  public static NoteAttachmentResourceRestTestClient noAuth() {
    return new NoteAttachmentResourceRestTestClient(null);
  }

  public static NoteAttachmentResourceRestTestClient auth(String token) {
    return new NoteAttachmentResourceRestTestClient(token);
  }

  private String getAttachmentPath(String attachmentId) {
    return basePath + "/" + attachmentId;
  }

  public Response createAttachment(
      UUID noteId, String fileName, String contentType, byte[] fileContent) {
    return given()
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .headers(authorizationHeaders)
        .multiPart("noteId", noteId.toString())
        .multiPart("fileName", fileName)
        .multiPart("contentType", contentType)
        .multiPart("file", "file.txt", fileContent, contentType)
        .when()
        .post(basePath);
  }

  public Response getAttachmentMeta(UUID attachmentId) {
    return given()
        .headers(authorizationHeaders)
        .header("Accept", MediaType.APPLICATION_JSON)
        .when()
        .get(getAttachmentPath(attachmentId.toString()));
  }

  public Response downloadAttachmentContent(UUID attachmentId) {
    return given()
        .headers(authorizationHeaders)
        .header("Accept", MediaType.APPLICATION_OCTET_STREAM)
        .when()
        .get(getAttachmentPath(attachmentId.toString()));
  }

  public Response getAttachmentsForNote(UUID noteId) {
    return given().headers(authorizationHeaders).queryParam("noteId", noteId).when().get(basePath);
  }

  public Response deleteAttachment(UUID attachmentId) {
    return given()
        .headers(authorizationHeaders)
        .when()
        .delete(getAttachmentPath(attachmentId.toString()));
  }
}
