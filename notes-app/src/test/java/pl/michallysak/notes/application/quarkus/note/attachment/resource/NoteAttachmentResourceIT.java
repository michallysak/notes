package pl.michallysak.notes.application.quarkus.note.attachment.resource;

import static org.junit.jupiter.api.Assertions.*;
import static pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils.createSetNotePermissionsRequestBuilder;
import static pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils.getCreateNoteRequestBuilder;
import static pl.michallysak.notes.helpers.TestExtensions.toJsonString;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.quarkus.helpers.BaseIT;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.AttachmentResponse;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.SetNotePermissionsRequest;
import pl.michallysak.notes.application.quarkus.note.resource.NoteResourceRestTestClient;
import pl.michallysak.notes.note.model.NotePermission;

@QuarkusTest
class NoteAttachmentResourceIT extends BaseIT {

  private static final String SAMPLE_FILE_NAME = "test.txt";
  private static final String SAMPLE_CONTENT_TYPE = "text/plain";
  private static final byte[] SAMPLE_FILE_CONTENT = "Hello World".getBytes();

  @Test
  void createAttachment_shouldReturn201AndAttachmentResponse() {
    // given
    String token = createUser(EMAIL_1);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(token, createNoteRequest);
    NoteAttachmentResourceRestTestClient attachmentClient =
        NoteAttachmentResourceRestTestClient.auth(token);
    // when
    Response response =
        attachmentClient.createAttachment(
            UUID.fromString(noteId), SAMPLE_FILE_NAME, SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT);
    // then
    response.then().statusCode(201);
    // and
    AttachmentResponse attachmentResponse = response.as(AttachmentResponse.class);
    assertEquals(SAMPLE_FILE_NAME, attachmentResponse.getFileName());
    assertEquals(SAMPLE_CONTENT_TYPE, attachmentResponse.getContentType());
    assertEquals(SAMPLE_FILE_CONTENT.length, attachmentResponse.getSize());
    assertEquals(UUID.fromString(noteId), attachmentResponse.getNoteId());
    assertEquals(getUserId(token), attachmentResponse.getAuthorId());
    assertNotNull(attachmentResponse.getId());
    assertNotNull(attachmentResponse.getCreated());
  }

  @Test
  void createAttachment_shouldReturn401_whenNoAuth() {
    // given
    NoteAttachmentResourceRestTestClient attachmentClient =
        NoteAttachmentResourceRestTestClient.noAuth();
    // when
    Response response =
        attachmentClient.createAttachment(
            UUID.randomUUID(), SAMPLE_FILE_NAME, SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT);
    // then
    response.then().statusCode(401);
  }

  @Test
  void getAttachmentMeta_shouldReturn200AndAttachmentResponse() {
    // given
    String token = createUser(EMAIL_1);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(token, createNoteRequest);
    NoteAttachmentResourceRestTestClient attachmentClient =
        NoteAttachmentResourceRestTestClient.auth(token);
    String attachmentId =
        attachmentClient
            .createAttachment(
                UUID.fromString(noteId), SAMPLE_FILE_NAME, SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT)
            .then()
            .statusCode(201)
            .extract()
            .path("id");
    // when
    Response response = attachmentClient.getAttachmentMeta(UUID.fromString(attachmentId));
    // then
    response.then().statusCode(200);
    AttachmentResponse attachmentResponse = response.as(AttachmentResponse.class);
    assertEquals(UUID.fromString(attachmentId), attachmentResponse.getId());
    assertEquals(SAMPLE_FILE_NAME, attachmentResponse.getFileName());
    assertEquals(SAMPLE_CONTENT_TYPE, attachmentResponse.getContentType());
  }

  @Test
  void getAttachmentMeta_shouldReturn401_whenNoAuth() {
    // given
    NoteAttachmentResourceRestTestClient attachmentClient =
        NoteAttachmentResourceRestTestClient.noAuth();
    // when
    Response response = attachmentClient.getAttachmentMeta(UUID.randomUUID());
    // then
    response.then().statusCode(401);
  }

  @Test
  void getAttachmentMeta_shouldReturn403_whenNonOwnerWithoutPermission() {
    // given
    String owner = createUser(EMAIL_1);
    String nonOwner = createUser(EMAIL_2);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(owner, createNoteRequest);
    NoteAttachmentResourceRestTestClient ownerAttachmentClient =
        NoteAttachmentResourceRestTestClient.auth(owner);
    String attachmentId =
        ownerAttachmentClient
            .createAttachment(
                UUID.fromString(noteId), SAMPLE_FILE_NAME, SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT)
            .then()
            .statusCode(201)
            .extract()
            .path("id");
    NoteAttachmentResourceRestTestClient nonOwnerAttachmentClient =
        NoteAttachmentResourceRestTestClient.auth(nonOwner);
    // when
    Response response = nonOwnerAttachmentClient.getAttachmentMeta(UUID.fromString(attachmentId));
    // then
    response.then().statusCode(403);
  }

  @Test
  void getAttachmentsForNote_shouldReturnListOfAttachments() {
    // given
    String token = createUser(EMAIL_1);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(token, createNoteRequest);
    NoteAttachmentResourceRestTestClient attachmentClient =
        NoteAttachmentResourceRestTestClient.auth(token);
    // Create two attachments
    attachmentClient.createAttachment(
        UUID.fromString(noteId), "file1.txt", SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT);
    attachmentClient.createAttachment(
        UUID.fromString(noteId), "file2.txt", SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT);
    // when
    Response response = attachmentClient.getAttachmentsForNote(UUID.fromString(noteId));
    // then
    response.then().statusCode(200);
    AttachmentResponse[] attachments = response.as(AttachmentResponse[].class);
    assertEquals(2, attachments.length);
    List<String> list =
        Arrays.stream(attachments).map(AttachmentResponse::getFileName).sorted().toList();
    assertEquals("file1.txt", list.get(0));
    assertEquals("file2.txt", list.get(1));
  }

  @Test
  void getAttachmentsForNote_shouldReturnEmptyList_whenNoAttachments() {
    // given
    String token = createUser(EMAIL_1);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(token, createNoteRequest);
    NoteAttachmentResourceRestTestClient attachmentClient =
        NoteAttachmentResourceRestTestClient.auth(token);
    // when
    Response response = attachmentClient.getAttachmentsForNote(UUID.fromString(noteId));
    // then
    response.then().statusCode(200);
    AttachmentResponse[] attachments = response.as(AttachmentResponse[].class);
    assertEquals(0, attachments.length);
  }

  @Test
  void getAttachmentsForNote_shouldReturnAttachments_whenOwnerListsAttachmentUploadedByEditor() {
    // given
    // owner shares the note with an editor who then uploads an attachment
    String ownerToken = createUser(EMAIL_1);
    String editorToken = createUser(EMAIL_2);
    UUID editorId = getUserId(editorToken);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(ownerToken, createNoteRequest);
    SetNotePermissionsRequest permissionsRequest =
        createSetNotePermissionsRequestBuilder()
            .email(EMAIL_2)
            .permissions(Set.of(NotePermission.EDIT))
            .build();
    NoteResourceRestTestClient.auth(ownerToken)
        .setPermissions(noteId, toJsonString(permissionsRequest))
        .then()
        .statusCode(204);
    NoteAttachmentResourceRestTestClient editorAttachmentClient =
        NoteAttachmentResourceRestTestClient.auth(editorToken);
    editorAttachmentClient
        .createAttachment(
            UUID.fromString(noteId), SAMPLE_FILE_NAME, SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT)
        .then()
        .statusCode(201);
    NoteAttachmentResourceRestTestClient ownerAttachmentClient =
        NoteAttachmentResourceRestTestClient.auth(ownerToken);
    // when
    Response response = ownerAttachmentClient.getAttachmentsForNote(UUID.fromString(noteId));
    // then
    // the owner can read the editor-authored attachment meta
    response.then().statusCode(200);
    AttachmentResponse[] attachments = response.as(AttachmentResponse[].class);
    assertEquals(1, attachments.length);
    assertEquals(SAMPLE_FILE_NAME, attachments[0].getFileName());
    assertEquals(editorId, attachments[0].getAuthorId());
  }

  @Test
  void downloadAttachmentContent_shouldReturn200AndBinaryContent() {
    // given
    String token = createUser(EMAIL_1);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(token, createNoteRequest);
    NoteAttachmentResourceRestTestClient attachmentClient =
        NoteAttachmentResourceRestTestClient.auth(token);
    String attachmentId =
        attachmentClient
            .createAttachment(
                UUID.fromString(noteId), SAMPLE_FILE_NAME, SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT)
            .then()
            .statusCode(201)
            .extract()
            .path("id");
    // when
    Response response = attachmentClient.downloadAttachmentContent(UUID.fromString(attachmentId));
    // then
    response.then().statusCode(200);
    byte[] content = response.asByteArray();
    assertArrayEquals(SAMPLE_FILE_CONTENT, content);
  }

  @Test
  void downloadAttachmentContent_shouldReturn401_whenNoAuth() {
    // given
    NoteAttachmentResourceRestTestClient attachmentClient =
        NoteAttachmentResourceRestTestClient.noAuth();
    // when
    Response response = attachmentClient.downloadAttachmentContent(UUID.randomUUID());
    // then
    response.then().statusCode(401);
  }

  @Test
  void downloadAttachmentContent_shouldReturn403_whenNonOwnerWithoutPermission() {
    // given
    String owner = createUser(EMAIL_1);
    String nonOwner = createUser(EMAIL_2);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(owner, createNoteRequest);
    NoteAttachmentResourceRestTestClient ownerAttachmentClient =
        NoteAttachmentResourceRestTestClient.auth(owner);
    String attachmentId =
        ownerAttachmentClient
            .createAttachment(
                UUID.fromString(noteId), SAMPLE_FILE_NAME, SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT)
            .then()
            .statusCode(201)
            .extract()
            .path("id");
    NoteAttachmentResourceRestTestClient nonOwnerAttachmentClient =
        NoteAttachmentResourceRestTestClient.auth(nonOwner);
    // when
    Response response =
        nonOwnerAttachmentClient.downloadAttachmentContent(UUID.fromString(attachmentId));
    // then
    response.then().statusCode(403);
  }

  @Test
  void deleteAttachment_shouldReturn204() {
    // given
    String token = createUser(EMAIL_1);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(token, createNoteRequest);
    NoteAttachmentResourceRestTestClient attachmentClient =
        NoteAttachmentResourceRestTestClient.auth(token);
    String attachmentId =
        attachmentClient
            .createAttachment(
                UUID.fromString(noteId), SAMPLE_FILE_NAME, SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT)
            .then()
            .statusCode(201)
            .extract()
            .path("id");
    // when
    Response response = attachmentClient.deleteAttachment(UUID.fromString(attachmentId));
    // then
    response.then().statusCode(204);
  }

  @Test
  void deleteAttachment_shouldReturn401_whenNoAuth() {
    // given
    NoteAttachmentResourceRestTestClient attachmentClient =
        NoteAttachmentResourceRestTestClient.noAuth();
    // when
    Response response = attachmentClient.deleteAttachment(UUID.randomUUID());
    // then
    response.then().statusCode(401);
  }

  @Test
  void deleteAttachment_shouldReturn403_whenNonOwnerWithoutPermission() {
    // given
    String owner = createUser(EMAIL_1);
    String nonOwner = createUser(EMAIL_2);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(owner, createNoteRequest);
    NoteAttachmentResourceRestTestClient ownerAttachmentClient =
        NoteAttachmentResourceRestTestClient.auth(owner);
    String attachmentId =
        ownerAttachmentClient
            .createAttachment(
                UUID.fromString(noteId), SAMPLE_FILE_NAME, SAMPLE_CONTENT_TYPE, SAMPLE_FILE_CONTENT)
            .then()
            .statusCode(201)
            .extract()
            .path("id");
    NoteAttachmentResourceRestTestClient nonOwnerAttachmentClient =
        NoteAttachmentResourceRestTestClient.auth(nonOwner);
    // when
    Response response = nonOwnerAttachmentClient.deleteAttachment(UUID.fromString(attachmentId));
    // then
    response.then().statusCode(403);
  }
}
