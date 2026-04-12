package pl.michallysak.notes.application.quarkus.note.resource;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.sse.InboundSseEvent;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
    boolean awaitResult =
        notesEventsSseTestClient.runWithContext(
            (source, ctx) -> {
              source.open();
              try {
                return ctx.getErrorLatch().await(2, TimeUnit.SECONDS);
              } catch (InterruptedException e) {
                fail("Test was interrupted while waiting for error callback", e);
                return null;
              }
            });

    // then
    assertTrue(awaitResult);
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
    CountDownLatch openLatch = new CountDownLatch(1);
    String jwt = createUser(EMAIL_1);
    NotesEventsSseTestClient notesEventsSseTestClient = NotesEventsSseTestClient.auth(jwt);
    // when
    boolean awaitResult =
        notesEventsSseTestClient.runWithContext(
            (source, ctx) -> {
              source.open();
              new Thread(
                      () -> {
                        try {
                          Thread.sleep(500);
                          if (source.isOpen()) {
                            openLatch.countDown();
                          }
                        } catch (InterruptedException ignored) {
                        }
                      })
                  .start();
              try {
                return openLatch.await(2, TimeUnit.SECONDS);
              } catch (InterruptedException e) {
                fail("Test was interrupted while waiting for open callback", e);
                return false;
              }
            });
    // then
    assertTrue(awaitResult);
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
    boolean awaitResult =
        notesEventsSseTestClient.runWithContext(
            (source, ctx) -> {
              source.open();
              CreateNoteRequest createNoteRequest =
                  NoteDtoRequestUtils.getCreateNoteRequestBuilder().build();
              createNote(jwt, createNoteRequest);
              try {
                return ctx.getEventLatch().await(2, TimeUnit.SECONDS);
              } catch (InterruptedException e) {
                fail("Test was interrupted while waiting for SSE event", e);
                return false;
              }
            });
    // then
    assertTrue(awaitResult);
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
    boolean awaitResult =
        notesEventsSseTestClient.runWithContext(
            (source, ctx) -> {
              source.open();
              CreateNoteRequest createNoteRequest =
                  NoteDtoRequestUtils.getCreateNoteRequestBuilder().build();
              createNote(jwt2, createNoteRequest);
              try {
                return !ctx.getEventLatch().await(2, TimeUnit.SECONDS);
              } catch (InterruptedException e) {
                fail("Test was interrupted while waiting for SSE event", e);
                return true;
              }
            });
    // then
    assertTrue(awaitResult, "Should NOT receive SSE message for note created by another user");
    // exceptions
    assertTrue(notesEventsSseTestClient.getExceptions().isEmpty());
    // events
    assertTrue(notesEventsSseTestClient.getEvents().isEmpty());
  }
}
