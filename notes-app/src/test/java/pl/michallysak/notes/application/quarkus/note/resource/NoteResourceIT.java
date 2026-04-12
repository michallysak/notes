package pl.michallysak.notes.application.quarkus.note.resource;

import static org.junit.jupiter.api.Assertions.*;
import static pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils.createNoteUpdateRequestBuilder;
import static pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils.getCreateNoteRequestBuilder;
import static pl.michallysak.notes.helpers.TestExtensions.toJsonString;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.quarkus.helpers.BaseIT;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;

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
}
