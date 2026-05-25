package pl.michallysak.notes.application.quarkus.note.resource;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static pl.michallysak.notes.helpers.TestExtensions.toJsonString;
import static pl.michallysak.notes.helpers.TestExtensions.waitGivenMillis;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.InboundSseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.quarkus.helpers.BaseIT;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils;
import pl.michallysak.notes.application.quarkus.note.dto.SetNotePermissionsRequest;
import pl.michallysak.notes.note.model.NoteValue;

@QuarkusTest
class NoteSseResourceIT extends BaseIT {

  private static final Set<String> ALL_EVENTS;
  private static final Set<String> NOTE_EVENTS =
      Set.of("NOTE_CREATED_EVENT", "NOTE_UPDATED_EVENT", "NOTE_DELETED_EVENT");
  private static final Set<String> PERMISSION_EVENTS =
      Set.of("NOTE_PERMISSIONS_SET_EVENT", "NOTE_ACCESS_REMOVED_EVENT");

  static {
    ALL_EVENTS = new HashSet<>();
    ALL_EVENTS.addAll(NOTE_EVENTS);
    ALL_EVENTS.addAll(PERMISSION_EVENTS);
  }

  @Test
  void createStreamKey_shouldReturn401_whenNotAuthenticated() {
    // given
    String jwt = null;
    Set<String> noteEvents = ALL_EVENTS;
    // when
    Response authorization = createStreamKeyResponse(jwt, noteEvents);
    // then
    authorization.then().statusCode(401);
  }

  @Test
  void createStreamKey_shouldReturn400_whenEventsEmpty() {
    // given
    String jwt = createUser(EMAIL_1);
    Set<String> noteEvents = Set.of();
    // when
    Response authorization = createStreamKeyResponse(jwt, noteEvents);
    // then
    authorization.then().statusCode(400);
  }

  @Test
  void createStreamKey_shouldReturn201_whenAuthenticatedWithEvents() {
    // given
    String jwt = createUser(EMAIL_1);
    // when
    String key = createStreamKey(jwt, ALL_EVENTS);
    // then
    assertNotNull(key);
    assertFalse(key.isEmpty());
  }

  @Test
  void connectToNotesEvents_shouldConnect_whenValidKey() {
    // given
    String jwt = createUser(EMAIL_1);
    String key = createStreamKey(jwt, ALL_EVENTS);
    NotesEventsSseTestClient notesEventsSseTestClient = NotesEventsSseTestClient.withKey(key);
    // when
    notesEventsSseTestClient.runWithContext(
        (source, ctx) -> {
          source.open();
          waitGivenMillis(200);
          assertTrue(source.isOpen());
        });
    // then
    assertTrue(notesEventsSseTestClient.getExceptions().isEmpty());
    assertTrue(notesEventsSseTestClient.getEvents().isEmpty());
  }

  @Test
  void connectToNotesEvents_shouldReceiveSseMessage_whenNoteCreatedByAuthenticatedUser() {
    // given
    String jwt = createUser(EMAIL_1);
    String key = createStreamKey(jwt, ALL_EVENTS);
    NotesEventsSseTestClient notesEventsSseTestClient = NotesEventsSseTestClient.withKey(key);
    // when
    notesEventsSseTestClient.runWithContext(
        (source, ctx) -> {
          source.open();
          CreateNoteRequest createNoteRequest =
              NoteDtoRequestUtils.getCreateNoteRequestBuilder().build();
          createNote(jwt, createNoteRequest);
          waitGivenMillis(200);
        });
    // then
    // exceptions
    assertTrue(notesEventsSseTestClient.getExceptions().isEmpty());
    // events
    assertFalse(notesEventsSseTestClient.getEvents().isEmpty());
    InboundSseEvent sseEvent = notesEventsSseTestClient.getEvents().getFirst();
    assertDoesNotThrow(() -> UUID.fromString(sseEvent.getId()));
    assertEquals("NOTE_CREATED_EVENT", sseEvent.getName());
    String msg = sseEvent.readData();
    try {
      NoteValue payload = OBJECT_MAPPER.readValue(msg, NoteValue.class);
      assertNotNull(payload);
      assertNotNull(payload.id());
      assertNotNull(payload.title());
      assertNotNull(payload.content());
      assertNotNull(payload.created());
      assertNull(payload.updated());
      assertNotNull(payload.authorId());
      assertFalse(payload.pinned());
    } catch (JsonProcessingException e) {
      fail("JSON parsing failed. Payload: " + msg, e);
    }
  }

  @Test
  void connectToNotesEvents_shouldNotReceiveSseEvent_whenNoteCreatedByOtherUser() {
    // given
    String jwt1 = createUser(EMAIL_1);
    String jwt2 = createUser(EMAIL_2);
    String key = createStreamKey(jwt1, ALL_EVENTS);
    NotesEventsSseTestClient notesEventsSseTestClient = NotesEventsSseTestClient.withKey(key);
    // when
    notesEventsSseTestClient.runWithContext(
        (source, ctx) -> {
          source.open();
          CreateNoteRequest createNoteRequest =
              NoteDtoRequestUtils.getCreateNoteRequestBuilder().build();
          createNote(jwt2, createNoteRequest);
          waitGivenMillis(200);
        });
    // then
    // exceptions
    assertTrue(notesEventsSseTestClient.getExceptions().isEmpty());
    // events
    assertTrue(
        notesEventsSseTestClient.getEvents().isEmpty(),
        "Should NOT receive SSE message for note created by another user");
  }

  @Test
  void connectToNotesEvents_shouldFail_whenInvalidKey() {
    // given
    NotesEventsSseTestClient notesEventsSseTestClient =
        NotesEventsSseTestClient.withKey("invalid-key");
    // when
    notesEventsSseTestClient.runWithContext(
        (source, ctx) -> {
          source.open();
          waitGivenMillis(200);
        });
    // then
    assertFalse(notesEventsSseTestClient.getExceptions().isEmpty());
  }

  @Test
  void connectToNotesEvents_shouldReceiveSseMessage_whenPermissionsSetByOwner() {
    // given
    String jwtOwner = createUser(EMAIL_1);
    String jwtTarget = createUser(EMAIL_2);
    String key = createStreamKey(jwtTarget, PERMISSION_EVENTS);
    NotesEventsSseTestClient notesEventsSseTestClient = NotesEventsSseTestClient.withKey(key);
    // when
    notesEventsSseTestClient.runWithContext(
        (source, ctx) -> {
          source.open();
          CreateNoteRequest createNoteRequest =
              NoteDtoRequestUtils.getCreateNoteRequestBuilder().build();
          String noteId = createNote(jwtOwner, createNoteRequest);
          NoteResourceRestTestClient noteClient = NoteResourceRestTestClient.auth(jwtOwner);
          SetNotePermissionsRequest permissionsRequest =
              NoteDtoRequestUtils.createSetNotePermissionsRequestBuilder().email(EMAIL_2).build();
          noteClient
              .setPermissions(noteId, toJsonString(permissionsRequest))
              .then()
              .statusCode(204);
          waitGivenMillis(200);
        });
    // then
    // exceptions
    assertTrue(notesEventsSseTestClient.getExceptions().isEmpty());
    // events
    assertFalse(notesEventsSseTestClient.getEvents().isEmpty());
    InboundSseEvent sseEvent = notesEventsSseTestClient.getEvents().getFirst();
    assertDoesNotThrow(() -> UUID.fromString(sseEvent.getId()));
    assertEquals("NOTE_PERMISSIONS_SET_EVENT", sseEvent.getName());
    assertNotNull(sseEvent.readData());
  }

  @Test
  void connectToNotesEvents_shouldReceiveSseMessage_whenAccessRemovedByOwner() {
    // given
    String jwtOwner = createUser(EMAIL_1);
    String jwtTarget = createUser(EMAIL_2);
    String key = createStreamKey(jwtTarget, PERMISSION_EVENTS);
    NotesEventsSseTestClient notesEventsSseTestClient = NotesEventsSseTestClient.withKey(key);
    // when
    notesEventsSseTestClient.runWithContext(
        (source, ctx) -> {
          source.open();
          CreateNoteRequest createNoteRequest =
              NoteDtoRequestUtils.getCreateNoteRequestBuilder().build();
          String noteId = createNote(jwtOwner, createNoteRequest);
          NoteResourceRestTestClient noteClient = NoteResourceRestTestClient.auth(jwtOwner);
          // First set permissions
          SetNotePermissionsRequest permissionsRequest =
              NoteDtoRequestUtils.createSetNotePermissionsRequestBuilder().email(EMAIL_2).build();
          noteClient
              .setPermissions(noteId, toJsonString(permissionsRequest))
              .then()
              .statusCode(204);
          waitGivenMillis(100);
          // Clear events from setPermissions
          notesEventsSseTestClient.getEvents().clear();
          // Now remove access
          UUID targetUserId = getUserId(jwtTarget);
          noteClient.removeAccess(noteId, targetUserId).then().statusCode(204);
          waitGivenMillis(200);
        });
    // then
    // exceptions
    assertTrue(notesEventsSseTestClient.getExceptions().isEmpty());
    // events
    assertFalse(notesEventsSseTestClient.getEvents().isEmpty());
    InboundSseEvent sseEvent = notesEventsSseTestClient.getEvents().getFirst();
    assertDoesNotThrow(() -> UUID.fromString(sseEvent.getId()));
    assertEquals("NOTE_ACCESS_REMOVED_EVENT", sseEvent.getName());
    assertNotNull(sseEvent.readData());
  }

  @Test
  void connectToNotesEvents_shouldNotReceivePermissionEvent_whenSetByOtherUser() {
    // given
    String jwtUser1 = createUser(EMAIL_1);
    String jwtUser2 = createUser(EMAIL_2);
    String jwtUser3 = createUser(EMAIL_3);
    String key = createStreamKey(jwtUser1, PERMISSION_EVENTS);
    NotesEventsSseTestClient notesEventsSseTestClient = NotesEventsSseTestClient.withKey(key);
    // when
    notesEventsSseTestClient.runWithContext(
        (source, ctx) -> {
          source.open();
          CreateNoteRequest createNoteRequest =
              NoteDtoRequestUtils.getCreateNoteRequestBuilder().build();
          String noteId = createNote(jwtUser2, createNoteRequest);
          NoteResourceRestTestClient noteClient = NoteResourceRestTestClient.auth(jwtUser2);
          SetNotePermissionsRequest permissionsRequest =
              NoteDtoRequestUtils.createSetNotePermissionsRequestBuilder().email(EMAIL_3).build();
          noteClient
              .setPermissions(noteId, toJsonString(permissionsRequest))
              .then()
              .statusCode(204);
          waitGivenMillis(200);
        });
    // then
    // exceptions
    assertTrue(notesEventsSseTestClient.getExceptions().isEmpty());
    // events - should be empty since user1 is not involved in the permission change
    assertTrue(
        notesEventsSseTestClient.getEvents().isEmpty(),
        "Should NOT receive permission event for note by another user");
  }

  private String createStreamKey(String jwt, Set<String> events) {
    return createStreamKeyResponse(jwt, events).then().statusCode(201).extract().path("key");
  }

  private Response createStreamKeyResponse(String jwt, Set<String> events) {
    return given()
        .header("Authorization", "Bearer " + jwt)
        .contentType(MediaType.APPLICATION_JSON)
        .body(toJsonString(events))
        .when()
        .post("/notes/events");
  }
}
