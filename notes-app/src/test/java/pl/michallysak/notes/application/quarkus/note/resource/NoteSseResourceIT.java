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
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.quarkus.helpers.BaseIT;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils;
import pl.michallysak.notes.note.model.NoteValue;

@QuarkusTest
class NoteSseResourceIT extends BaseIT {

  private static final Set<String> NOTE_EVENTS = Set.of("NOTE_CREATED_EVENT");

  @Test
  void createStreamKey_shouldReturn401_whenNotAuthenticated() {
    // given
    String jwt = null;
    Set<String> noteEvents = NOTE_EVENTS;
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
    String key = createStreamKey(jwt, NOTE_EVENTS);
    // then
    assertNotNull(key);
    assertFalse(key.isEmpty());
  }

  @Test
  void connectToNotesEvents_shouldConnect_whenValidKey() {
    // given
    String jwt = createUser(EMAIL_1);
    String key = createStreamKey(jwt, NOTE_EVENTS);
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
    String key = createStreamKey(jwt, NOTE_EVENTS);
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
    String key = createStreamKey(jwt1, NOTE_EVENTS);
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
