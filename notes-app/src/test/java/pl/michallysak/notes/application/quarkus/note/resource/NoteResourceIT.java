package pl.michallysak.notes.application.quarkus.note.resource;

import static org.junit.jupiter.api.Assertions.*;
import static pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils.createNoteUpdateRequestBuilder;
import static pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils.createSetNotePermissionsRequestBuilder;
import static pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils.getCreateNoteRequestBuilder;
import static pl.michallysak.notes.helpers.TestExtensions.toJsonString;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.quarkus.helpers.BaseIT;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteShareResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;
import pl.michallysak.notes.application.quarkus.note.dto.SetNotePermissionsRequest;
import pl.michallysak.notes.application.quarkus.user.resource.UserResourceRestTestClient;
import pl.michallysak.notes.note.model.NotePermission;

@QuarkusTest
class NoteResourceIT extends BaseIT {

  @Test
  void createNote_shouldReturn201AndLocationAndBody() {
    // given
    String token = createUser(EMAIL_1);
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.auth(token);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    // when
    Response response = noteResourceTestClient.createNote(toJsonString(createNoteRequest));
    // then
    response.then().statusCode(201);
    // and
    NoteResponse noteResponse = response.as(NoteResponse.class);
    assertEquals(createNoteRequest.getTitle(), noteResponse.getTitle());
    assertEquals(createNoteRequest.getContent(), noteResponse.getContent());
    assertNotNull(noteResponse.getId());
    assertNotNull(noteResponse.getCreated());
    assertNull(noteResponse.getUpdated());
    assertFalse(noteResponse.isPinned());
  }

  @Test
  void createNote_shouldReturn401_whenNoAuth() {
    // given
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.noAuth();
    String body = toJsonString(getCreateNoteRequestBuilder().build());
    // when
    Response response = noteResourceTestClient.createNote(body);
    // then
    response.then().statusCode(401);
  }

  @Test
  void createNote_shouldReturn400_whenValidationException() {
    // given
    String token = createUser(EMAIL_1);
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.auth(token);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().title(null).build();
    // when
    Response response = noteResourceTestClient.createNote(toJsonString(createNoteRequest));
    // then
    response.then().statusCode(400);
    assertTrue(response.asString().contains("Title cannot be null"));
  }

  @Test
  void getNotes_shouldReturnListWithCreatedNote() {
    // given
    String token = createUser(EMAIL_1);
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.auth(token);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(token, createNoteRequest);
    // when
    Response response = noteResourceTestClient.getNotes();
    // then
    response.then().statusCode(200);
    NoteResponse[] noteResponses = response.as(NoteResponse[].class);
    assertEquals(1, noteResponses.length);
    // and
    NoteResponse noteResponse = noteResponses[0];
    assertEquals(noteId, noteResponse.getId().toString());
    assertEquals(createNoteRequest.getTitle(), noteResponse.getTitle());
    assertEquals(createNoteRequest.getContent(), noteResponse.getContent());
    assertNotNull(noteResponse.getCreated());
    assertNull(noteResponse.getUpdated());
    assertFalse(noteResponse.isPinned());
  }

  @Test
  void getNotes_shouldReturn401_whenNoAuth() {
    // given
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.noAuth();
    // when
    Response response = noteResourceTestClient.getNotes();
    // then
    response.then().statusCode(401);
  }

  @Test
  void getNote_shouldReturn200AndNote() {
    // given
    String token = createUser(EMAIL_1);
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.auth(token);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(token, createNoteRequest);
    // when
    Response response = noteResourceTestClient.getNote(noteId);
    // then
    response.then().statusCode(200);
    // and
    NoteResponse note = response.as(NoteResponse.class);
    assertEquals(noteId, note.getId().toString());
    assertEquals(createNoteRequest.getTitle(), note.getTitle());
    assertEquals(createNoteRequest.getContent(), note.getContent());
    assertNotNull(note.getCreated());
    assertNull(note.getUpdated());
  }

  @Test
  void getNote_shouldReturn401_whenNoAuth() {
    // given
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.noAuth();
    // when
    Response response = noteResourceTestClient.getNote("note-id");
    // then
    response.then().statusCode(401);
  }

  @Test
  void getNote_shouldReturn404_whenNotExists() {
    // given
    String token = createUser(EMAIL_1);
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.auth(token);
    String nonExistentId = UUID.randomUUID().toString();
    // when
    Response response = noteResourceTestClient.getNote(nonExistentId);
    // then
    response.then().statusCode(404);
  }

  @Test
  void updateNote_shouldReturn200AndUpdatedBody() {
    // given
    String token = createUser(EMAIL_1);
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.auth(token);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(token, createNoteRequest);
    NoteUpdateRequest noteUpdateRequest =
        createNoteUpdateRequestBuilder()
            .title("newTitle")
            .content("newContent")
            .pinned(true)
            .build();
    // when
    Response response = noteResourceTestClient.updateNote(noteId, toJsonString(noteUpdateRequest));
    // then
    response.then().statusCode(200);
    // and
    NoteResponse noteResponse = response.as(NoteResponse.class);
    assertEquals(noteUpdateRequest.getTitle(), noteResponse.getTitle());
    assertEquals(noteUpdateRequest.getContent(), noteResponse.getContent());
    assertNotNull(noteResponse.getId());
    assertNotNull(noteResponse.getCreated());
    assertNotNull(noteResponse.getUpdated());
    assertTrue(noteResponse.isPinned());
  }

  @Test
  void updateNote_shouldReturn401_whenNoAuth() {
    // given
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.noAuth();
    String body = toJsonString(createNoteUpdateRequestBuilder().build());
    // when
    Response response = noteResourceTestClient.updateNote("note-id", body);
    // then
    response.then().statusCode(401);
  }

  @Test
  void updateNote_shouldReturn400_whenValidationException() {
    // given
    String token = createUser(EMAIL_1);
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.auth(token);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(token, createNoteRequest);
    NoteUpdateRequest noteUpdateRequest = createNoteUpdateRequestBuilder().title("X").build();
    // when
    Response response = noteResourceTestClient.updateNote(noteId, toJsonString(noteUpdateRequest));
    // then
    response.then().statusCode(400);
    assertTrue(response.asString().contains("Title not meet length requirements"));
  }

  @Test
  void updateNote_shouldReturn404_whenNotExists() {
    // given
    String token = createUser(EMAIL_1);
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.auth(token);
    String nonExistentId = UUID.randomUUID().toString();
    NoteUpdateRequest noteUpdateRequest = createNoteUpdateRequestBuilder().build();
    // when
    Response response =
        noteResourceTestClient.updateNote(nonExistentId, toJsonString(noteUpdateRequest));
    // then
    response.then().statusCode(404);
  }

  @Test
  void deleteNote_shouldReturn200AndDelete() {
    // given
    String token = createUser(EMAIL_1);
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.auth(token);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(token, createNoteRequest);
    // when
    Response deleteResponse = noteResourceTestClient.deleteNote(noteId);
    // then
    deleteResponse.then().statusCode(204);
  }

  @Test
  void deleteNote_shouldReturn401_whenNoAuth() {
    // given
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.noAuth();
    // when
    Response response = noteResourceTestClient.deleteNote("note-id");
    // then
    response.then().statusCode(401);
  }

  @Test
  void deleteNote_shouldReturn404_whenNotExists() {
    // given
    String token = createUser(EMAIL_1);
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.auth(token);
    String nonExistentId = UUID.randomUUID().toString();
    // when
    Response response = noteResourceTestClient.deleteNote(nonExistentId);
    // then
    response.then().statusCode(404);
  }

  @Test
  void setPermissions_shouldReturn204AndAllowSharedUserToReadNote() {
    // given
    String ownerToken = createUser(EMAIL_1);
    String sharedUserToken = createUser(EMAIL_2);
    NoteResourceRestTestClient ownerClient = NoteResourceRestTestClient.auth(ownerToken);
    NoteResourceRestTestClient sharedUserClient = NoteResourceRestTestClient.auth(sharedUserToken);
    String noteId = createNote(ownerToken, getCreateNoteRequestBuilder().build());
    SetNotePermissionsRequest request =
        createSetNotePermissionsRequestBuilder().email(EMAIL_2).build();
    // when
    Response response = ownerClient.setPermissions(noteId, toJsonString(request));
    // then
    response.then().statusCode(204);
    Response permissionsResponse = ownerClient.getPermissions(noteId);
    permissionsResponse.then().statusCode(200);
    NoteShareResponse[] permissions = permissionsResponse.as(NoteShareResponse[].class);
    assertEquals(1, permissions.length);
    assertEquals(EMAIL_2, permissions[0].getEmail());
    assertTrue(permissions[0].getPermissions().contains(NotePermission.READ));
    // and
    Response sharedUserGetResponse = sharedUserClient.getNote(noteId);
    sharedUserGetResponse.then().statusCode(200);
  }

  @Test
  void setPermissions_shouldReturn403_whenRequestingUserIsNotNoteOwner() {
    // given
    String ownerToken = createUser(EMAIL_1);
    String secondUserToken = createUser(EMAIL_2);
    String noteId = createNote(ownerToken, getCreateNoteRequestBuilder().build());
    SetNotePermissionsRequest request =
        createSetNotePermissionsRequestBuilder().email(EMAIL_2).build();
    // when
    Response response =
        NoteResourceRestTestClient.auth(secondUserToken)
            .setPermissions(noteId, toJsonString(request));
    // then
    response.then().statusCode(403);
  }

  @Test
  void setPermissions_shouldReturn400_whenPermissionsEmpty() {
    // given
    String ownerToken = createUser(EMAIL_1);
    createUser(EMAIL_2);
    String noteId = createNote(ownerToken, getCreateNoteRequestBuilder().build());
    SetNotePermissionsRequest request =
        createSetNotePermissionsRequestBuilder()
            .email(EMAIL_2)
            .permissions(java.util.Set.of())
            .build();
    // when
    Response response =
        NoteResourceRestTestClient.auth(ownerToken).setPermissions(noteId, toJsonString(request));
    // then
    response.then().statusCode(400);
  }

  @Test
  void setPermissions_shouldReturn400_whenTargetUserIsActingUser() {
    // given
    String ownerToken = createUser(EMAIL_1);
    String noteId = createNote(ownerToken, getCreateNoteRequestBuilder().build());
    SetNotePermissionsRequest request =
        createSetNotePermissionsRequestBuilder().email(EMAIL_1).build();
    // when
    Response response =
        NoteResourceRestTestClient.auth(ownerToken).setPermissions(noteId, toJsonString(request));
    // then
    response.then().statusCode(400);
    assertTrue(response.asString().contains("Cannot set permissions for yourself"));
  }

  @Test
  void setPermissions_shouldReturn404_whenTargetUserDoesNotExist() {
    // given
    String ownerToken = createUser(EMAIL_1);
    String noteId = createNote(ownerToken, getCreateNoteRequestBuilder().build());
    SetNotePermissionsRequest request =
        createSetNotePermissionsRequestBuilder().email("missing.user@test.pl").build();
    // when
    Response response =
        NoteResourceRestTestClient.auth(ownerToken).setPermissions(noteId, toJsonString(request));
    // then
    response.then().statusCode(404);
  }

  @Test
  void removeAccess_shouldReturn204AndRevokeSharedUserAccess() {
    // given
    String ownerToken = createUser(EMAIL_1);
    String sharedUserToken = createUser(EMAIL_2);
    NoteResourceRestTestClient ownerClient = NoteResourceRestTestClient.auth(ownerToken);
    NoteResourceRestTestClient sharedUserClient = NoteResourceRestTestClient.auth(sharedUserToken);
    String sharedUserId =
        UserResourceRestTestClient.auth(sharedUserToken)
            .me()
            .then()
            .statusCode(200)
            .extract()
            .path("id");
    String noteId = createNote(ownerToken, getCreateNoteRequestBuilder().build());
    UUID targetUserId = UUID.fromString(sharedUserId);
    SetNotePermissionsRequest request =
        createSetNotePermissionsRequestBuilder().email(EMAIL_2).build();
    ownerClient.setPermissions(noteId, toJsonString(request)).then().statusCode(204);
    // and
    Response permissionsBeforeResponse = ownerClient.getPermissions(noteId);
    permissionsBeforeResponse.then().statusCode(200);
    NoteShareResponse[] permissionsBefore = permissionsBeforeResponse.as(NoteShareResponse[].class);
    assertEquals(1, permissionsBefore.length);
    // when
    Response response = ownerClient.removeAccess(noteId, targetUserId);
    // then
    response.then().statusCode(204);
    // and
    Response permissionsAfterResponse = ownerClient.getPermissions(noteId);
    permissionsAfterResponse.then().statusCode(200);
    NoteShareResponse[] permissionsAfter = permissionsAfterResponse.as(NoteShareResponse[].class);
    assertEquals(0, permissionsAfter.length);
    // and
    sharedUserClient.getNote(noteId).then().statusCode(403);
  }

  @Test
  void removeAccess_shouldReturn401_whenNoAuth() {
    // given
    String randomId = UUID.randomUUID().toString();
    UUID targetUserId = UUID.randomUUID();
    // when
    Response response = NoteResourceRestTestClient.noAuth().removeAccess(randomId, targetUserId);
    // then
    response.then().statusCode(401);
  }

  @Test
  void removeAccess_shouldReturn404_whenTargetUserDoesNotExist() {
    // given
    String ownerToken = createUser(EMAIL_1);
    String noteId = createNote(ownerToken, getCreateNoteRequestBuilder().build());
    UUID nonExistentUserId = UUID.randomUUID();
    // when
    Response response =
        NoteResourceRestTestClient.auth(ownerToken).removeAccess(noteId, nonExistentUserId);
    // then
    response.then().statusCode(404);
  }

  @Test
  void getPermissions_shouldReturn200AndEmptyList_whenNoPermissionsSet() {
    // given
    String token = createUser(EMAIL_1);
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.auth(token);
    CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
    String noteId = createNote(token, createNoteRequest);
    // when
    Response response = noteResourceTestClient.getPermissions(noteId);
    // then
    response.then().statusCode(200);
    // and
    NoteShareResponse[] permissions = response.as(NoteShareResponse[].class);
    assertEquals(0, permissions.length);
  }

  @Test
  void getPermissions_shouldReturn200AndPermissionsList() {
    // given
    String ownerToken = createUser(EMAIL_1);
    createUser(EMAIL_2);
    createUser("test3@example.com");
    NoteResourceRestTestClient ownerClient = NoteResourceRestTestClient.auth(ownerToken);
    String noteId = createNote(ownerToken, getCreateNoteRequestBuilder().build());
    // and
    SetNotePermissionsRequest request1 =
        createSetNotePermissionsRequestBuilder().email(EMAIL_2).build();
    ownerClient.setPermissions(noteId, toJsonString(request1)).then().statusCode(204);
    // and
    SetNotePermissionsRequest request2 =
        createSetNotePermissionsRequestBuilder()
            .email("test3@example.com")
            .permissions(Set.of(NotePermission.READ))
            .build();
    ownerClient.setPermissions(noteId, toJsonString(request2)).then().statusCode(204);
    // when
    Response response = ownerClient.getPermissions(noteId);
    // then
    response.then().statusCode(200);
    // and
    NoteShareResponse[] permissions = response.as(NoteShareResponse[].class);
    assertEquals(2, permissions.length);
    assertTrue(Arrays.stream(permissions).anyMatch(p -> EMAIL_2.equals(p.getEmail())));
    assertTrue(Arrays.stream(permissions).anyMatch(p -> "test3@example.com".equals(p.getEmail())));
  }

  @Test
  void getPermissions_shouldReturn404_whenNoteNotExists() {
    // given
    String token = createUser(EMAIL_1);
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.auth(token);
    String nonExistentId = UUID.randomUUID().toString();
    // when
    Response response = noteResourceTestClient.getPermissions(nonExistentId);
    // then
    response.then().statusCode(404);
  }

  @Test
  void getPermissions_shouldReturn401_whenNoAuth() {
    // given
    NoteResourceRestTestClient noteResourceTestClient = NoteResourceRestTestClient.noAuth();
    String randomId = UUID.randomUUID().toString();
    // when
    Response response = noteResourceTestClient.getPermissions(randomId);
    // then
    response.then().statusCode(401);
  }
}
