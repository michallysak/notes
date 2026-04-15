package pl.michallysak.notes.application.quarkus.note.resource;

import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import pl.michallysak.notes.application.quarkus.helpers.SseTestClient;

public class NotesEventsSseTestClient extends SseTestClient {

  private static final String BASE_URI = "http://localhost:8081";
  private static final String PATH = "/notes/events";

  private NotesEventsSseTestClient(URI uri) {
    super(uri);
  }

  public static NotesEventsSseTestClient withKey(String key) {
    URI uri = UriBuilder.fromUri(BASE_URI).path(PATH).queryParam("key", key).build();
    return new NotesEventsSseTestClient(uri);
  }
}
