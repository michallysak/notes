package pl.michallysak.notes.application.quarkus.helpers;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.sse.InboundSseEvent;
import jakarta.ws.rs.sse.SseEventSource;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SseTestClient {
  @Getter private final List<Throwable> exceptions = new ArrayList<>();
  @Getter private final List<InboundSseEvent> events = new ArrayList<>();

  private final URI uri;

  /**
   * Runs the SSE test with the given context. The provided BiConsumer receives the SseEventSource
   * and this SseTestClient instance. The lambda should open the source, perform any actions (e.g.
   * trigger events), and perform assertions.
   */
  public void runWithContext(BiConsumer<SseEventSource, SseTestClient> onContext) {
    try (Client client = ClientBuilder.newClient()) {
      WebTarget target = client.target(uri);
      try (SseEventSource source = SseEventSource.target(target).build()) {
        source.register(this::onEventMsg, this::onError);
        onContext.accept(source, this);
      }
    }
  }

  private void onEventMsg(InboundSseEvent event) {
    System.out.printf(
        "[SSE] Received event:\nid: %s\nname: %s\ndata: %s%n",
        event.getId(), event.getName(), event.readData());
    events.add(event);
  }

  private void onError(Throwable e) {
    if (isSocketClosed(e)) {
      // Closing the SSE source may trigger a "Socket closed" callback; treat as expected noise.
      return;
    }
    System.err.printf("[SSE] Error occurred: %s%n", e.getMessage());
    exceptions.add(e);
  }

  private boolean isSocketClosed(Throwable e) {
    return e instanceof SocketException
        || (e.getMessage() != null && e.getMessage().toLowerCase().contains("socket closed"));
  }
}
