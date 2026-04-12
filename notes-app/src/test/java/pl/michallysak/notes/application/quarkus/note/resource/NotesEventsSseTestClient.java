package pl.michallysak.notes.application.quarkus.note.resource;

import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import pl.michallysak.notes.application.quarkus.helpers.SseTestClient;

public class NotesEventsSseTestClient extends SseTestClient {

  private static final URI URI =
      UriBuilder.fromUri("http://localhost:8081").path("/notes/events").build();

  private NotesEventsSseTestClient(String token) {
    super(URI, token);
  }

  public static NotesEventsSseTestClient noAuth() {
    return new NotesEventsSseTestClient(null);
  }

  public static NotesEventsSseTestClient auth(String token) {
    return new NotesEventsSseTestClient(token);
  }
}
