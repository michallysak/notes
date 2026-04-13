package pl.michallysak.notes.application.quarkus.note.resource;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.sse.InboundSseEvent;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.quarkus.helpers.BaseIT;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils;
import pl.michallysak.notes.note.domain.event.NoteCreatedEvent;

@QuarkusTest
class NoteSseResourceIT extends BaseIT {

  @Test
  void connectToNotesEvents_shouldReturn401_whenNotAuthenticated() {
    // given
    NotesEventsSseTestClient notesEventsSseTestClient = NotesEventsSseTestClient.noAuth();
    // when
    notesEventsSseTestClient.runWithContext(
        (source, ctx) -> {
          source.open();
          waitGivenMillis(200);
        });

    // then
    // exceptions
    assertEquals(1, notesEventsSseTestClient.getExceptions().size());
    Throwable ex = notesEventsSseTestClient.getExceptions().getFirst();
    String msg = ex.getMessage();
    assertTrue(msg.contains("401"));
    // events
    assertTrue(notesEventsSseTestClient.getEvents().isEmpty());
  }

  @Test
  void connectToNotesEvents_shouldReturn200_whenAuthenticated() {
    // given
    String jwt = createUser(EMAIL_1);
    NotesEventsSseTestClient notesEventsSseTestClient = NotesEventsSseTestClient.auth(jwt);
    // when
    notesEventsSseTestClient.runWithContext(
        (source, ctx) -> {
          source.open();
          waitGivenMillis(200);
          assertTrue(source.isOpen());
        });
    // then
    // exceptions
    assertTrue(notesEventsSseTestClient.getExceptions().isEmpty());
    // events
    assertTrue(notesEventsSseTestClient.getEvents().isEmpty());
  }

  @Test
  void connectToNotesEvents_shouldReceiveSseMessage_whenNoteCreatedByAuthenticatedUser() {
    // given
    String jwt = createUser(EMAIL_1);
    NotesEventsSseTestClient notesEventsSseTestClient = NotesEventsSseTestClient.auth(jwt);
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
      NoteCreatedEvent.Payload payload =
          OBJECT_MAPPER.readValue(msg, NoteCreatedEvent.Payload.class);
      assertNotNull(payload);
      assertNotNull(payload.getTitle());
      assertNotNull(payload.getContent());
    } catch (JsonProcessingException e) {
      fail("JSON parsing failed. Payload: " + msg, e);
    }
  }

  @Test
  void connectToNotesEvents_shouldNotReceiveSseEvent_whenNoteCreatedByOtherUser() {
    // given
    String jwt1 = createUser(EMAIL_1);
    String jwt2 = createUser(EMAIL_2);
    NotesEventsSseTestClient notesEventsSseTestClient = NotesEventsSseTestClient.auth(jwt1);
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

  private static void waitGivenMillis(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      fail("Test was interrupted while waiting for SSE event", e);
    }
  }
}
