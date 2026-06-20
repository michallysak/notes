package pl.michallysak.notes.application.quarkus.note.attachment.resource;

import static org.junit.jupiter.api.Assertions.*;
import static pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils.getCreateNoteRequestBuilder;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.quarkus.helpers.BaseIT;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.AttachmentResponse;

@QuarkusTest
class NoteAttachmentResourceIT extends BaseIT {

  private static final String SAMPLE_FILE_NAME = "file.jpg";
  private static final String SAMPLE_CONTENT_TYPE = "image/jpeg";
  private static final byte[] SAMPLE_FILE_CONTENT = "fake-image-bytes".getBytes();

  @Test
  void createAttachment_shouldReturn201AndAttachmentResponse() {
    // given
    String token = createUser(EMAIL_1);
    String noteId = createNote(token, getCreateNoteRequestBuilder().build());
    NoteAttachmentResourceRestTestClient client = NoteAttachmentResourceRestTestClient.auth(token);

    // when
    Response response =
        client.createAttachment(
            UUID.fromString(noteId), SAMPLE_FILE_NAME, SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT);
    // then
    response.then().statusCode(201);

    AttachmentResponse body = response.as(AttachmentResponse.class);
    assertEquals(SAMPLE_FILE_NAME, body.getFileName());
    assertEquals(SAMPLE_CONTENT_TYPE, body.getContentType());
    assertEquals(SAMPLE_FILE_CONTENT.length, body.getSize());
  }

  @Test
  void getAttachmentMeta_shouldReturn200AndAttachmentMetadataResponse() {
    // given
    String token = createUser(EMAIL_1);
    String noteId = createNote(token, getCreateNoteRequestBuilder().build());
    NoteAttachmentResourceRestTestClient client = NoteAttachmentResourceRestTestClient.auth(token);

    String attachmentId =
        client
            .createAttachment(
                UUID.fromString(noteId), SAMPLE_FILE_NAME, SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT)
            .then()
            .statusCode(201)
            .extract()
            .path("id");
    // when
    Response response = client.getAttachmentMeta(UUID.fromString(attachmentId));

    // then
    response.then().statusCode(200);

    AttachmentResponse body = response.as(AttachmentResponse.class);
    assertEquals(UUID.fromString(attachmentId), body.getId());
  }

  @Test
  void getAttachmentMetadataMeta_shouldReturn403_whenNonOwnerWithoutPermission() {
    // given
    String owner = createUser(EMAIL_1);
    String user = createUser(EMAIL_2);

    String noteId = createNote(owner, getCreateNoteRequestBuilder().build());

    NoteAttachmentResourceRestTestClient ownerClient =
        NoteAttachmentResourceRestTestClient.auth(owner);
    String attachmentId =
        ownerClient
            .createAttachment(
                UUID.fromString(noteId), SAMPLE_FILE_NAME, SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    NoteAttachmentResourceRestTestClient userClient =
        NoteAttachmentResourceRestTestClient.auth(user);

    // when
    Response response = userClient.getAttachmentMeta(UUID.fromString(attachmentId));

    // then
    response.then().statusCode(403);
  }

  @Test
  void getAttachmentsForNote_shouldReturnListOfAttachmentsMetadata() {
    // given
    String token = createUser(EMAIL_1);
    String noteId = createNote(token, getCreateNoteRequestBuilder().build());
    NoteAttachmentResourceRestTestClient client = NoteAttachmentResourceRestTestClient.auth(token);

    client
        .createAttachment(
            UUID.fromString(noteId), "file1.jpg", SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT)
        .then()
        .statusCode(201);

    client
        .createAttachment(
            UUID.fromString(noteId), "file2.jpg", SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT)
        .then()
        .statusCode(201);

    // when
    Response response = client.getAttachmentsForNote(UUID.fromString(noteId));

    // then
    response.then().statusCode(200);
    AttachmentResponse[] attachments = response.as(AttachmentResponse[].class);
    assertEquals(2, attachments.length);
  }

  @Test
  void downloadAttachmentContent_shouldReturn200AndBinaryContent() {
    // given
    String token = createUser(EMAIL_1);
    String noteId = createNote(token, getCreateNoteRequestBuilder().build());
    NoteAttachmentResourceRestTestClient client = NoteAttachmentResourceRestTestClient.auth(token);

    String attachmentId =
        client
            .createAttachment(
                UUID.fromString(noteId), SAMPLE_FILE_NAME, SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT)
            .then()
            .statusCode(201)
            .extract()
            .path("id");
    // when
    Response response = client.downloadAttachmentContent(UUID.fromString(attachmentId));

    // then
    response.then().statusCode(200);
    assertArrayEquals(SAMPLE_FILE_CONTENT, response.asByteArray());
  }

  @Test
  void deleteAttachment_shouldReturn204() {
    // given
    String token = createUser(EMAIL_1);
    String noteId = createNote(token, getCreateNoteRequestBuilder().build());
    NoteAttachmentResourceRestTestClient client = NoteAttachmentResourceRestTestClient.auth(token);

    String attachmentId =
        client
            .createAttachment(
                UUID.fromString(noteId), SAMPLE_FILE_NAME, SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT)
            .then()
            .statusCode(201)
            .extract()
            .path("id");
    // when
    Response response = client.deleteAttachment(UUID.fromString(attachmentId));

    // then
    response.then().statusCode(204);
  }
}
