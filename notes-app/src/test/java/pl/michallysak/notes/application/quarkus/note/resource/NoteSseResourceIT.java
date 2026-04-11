package pl.michallysak.notes.application.quarkus.note.resource;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
import static pl.michallysak.notes.helpers.TestExtensions.toJsonString;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.sse.InboundSseEvent;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils;
import pl.michallysak.notes.note.domain.event.NoteCreatedEvent;
import pl.michallysak.notes.user.repository.UserRepository;

@QuarkusTest
class NoteSseResourceIT {

  private static final String EMAIL_1 = "user1@test.pl";
  private static final String EMAIL_2 = "user2@test.pl";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Inject UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  void connectToNotesEvents_shouldReturn401_whenNotAuthenticated() {
    // given
    withNotesEventsSSE(
        "",
        ctx -> {
          ctx.open();
          // then
          try {
            boolean error = ctx.getErrorLatch().await(2, TimeUnit.SECONDS);
            assertTrue(error);
          } catch (InterruptedException e) {
            fail("Test was interrupted while waiting for error callback", e);
            return;
          }
          // exceptions
          assertEquals(1, ctx.getExceptions().size());
          Throwable ex = ctx.getExceptions().getFirst();
          String msg = ex.getMessage();
          assertTrue(msg.contains("401"));
          // events
          assertTrue(ctx.getEvents().isEmpty());
        });
  }

  @Test
  void connectToNotesEvents_shouldReturn200_whenAuthenticated() {
    // given
    CountDownLatch openLatch = new CountDownLatch(1);
    String jwt = createUser(EMAIL_1);
    withNotesEventsSSE(
        jwt,
        ctx -> {
          ctx.open();
          new Thread(
                  () -> {
                    try {
                      Thread.sleep(500);
                      if (ctx.isOpen()) {
                        openLatch.countDown();
                      }
                    } catch (InterruptedException ignored) {
                    }
                  })
              .start();
          // then
          try {
            boolean ok = openLatch.await(2, TimeUnit.SECONDS);
            assertTrue(ok);
          } catch (InterruptedException e) {
            fail("Test was interrupted while waiting for open callback", e);
            return;
          }
          // exceptions
          assertTrue(ctx.getExceptions().isEmpty());
          // events
          assertTrue(ctx.getEvents().isEmpty());
        });
  }

  @Test
  void connectToNotesEvents_shouldReceiveSseMessage_whenNoteCreatedByAuthenticatedUser() {
    // given
    String jwt = createUser(EMAIL_1);
    withNotesEventsSSE(
        jwt,
        ctx -> {
          // when
          ctx.open();
          createNote(jwt);
          // then
          try {
            boolean received = ctx.getEventLatch().await(5, TimeUnit.SECONDS);
            assertTrue(received);
          } catch (InterruptedException e) {
            fail("Test was interrupted while waiting for SSE event", e);
            return;
          }
          // exceptions
          assertTrue(ctx.getExceptions().isEmpty());
          // events
          assertFalse(ctx.getEvents().isEmpty());
          InboundSseEvent sseEvent = ctx.getEvents().getFirst();
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
        });
  }

  @Test
  void connectToNotesEvents_shouldNotReceiveSseEvent_whenNoteCreatedByOtherUser() {
    // given
    String jwt1 = createUser(EMAIL_1);
    String jwt2 = createUser(EMAIL_2);
    withNotesEventsSSE(
        jwt1,
        ctx -> {
          // when
          ctx.open();
          createNote(jwt2);
          // then
          try {
            boolean received = ctx.getEventLatch().await(3, TimeUnit.SECONDS);
            assertFalse(
                received, "Should NOT receive SSE message for note created by another user");
          } catch (InterruptedException e) {
            fail("Test was interrupted while waiting for SSE event", e);
          }
          // exceptions
          assertTrue(ctx.getExceptions().isEmpty());
          // events
          assertTrue(ctx.getEvents().isEmpty());
        });
  }

  private static String createUser(String email) {
    Response response =
        given()
            .contentType("application/json")
            .body("{\"email\": \"%s\",\"password\": \"%s\"}".formatted(email, "Pass123!"))
            .post("/users/register");
    response.then().statusCode(201);
    return response.jsonPath().getString("token");
  }

  private static void createNote(String jwt) {
    given()
        .contentType("application/json")
        .header("Authorization", "Bearer %s".formatted(jwt))
        .body(toJsonString(NoteDtoRequestUtils.getCreateNoteRequestBuilder().build()))
        .post("/notes")
        .then()
        .statusCode(201);
  }

  private static void withNotesEventsSSE(String token, Consumer<SseTestContext> onContext) {
    URI notesEvents = UriBuilder.fromUri("http://localhost:8081").path("/notes/events").build();
    SseTestContext.build(notesEvents, token, onContext);
  }
}
