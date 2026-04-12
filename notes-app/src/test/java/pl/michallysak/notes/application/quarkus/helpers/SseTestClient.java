package pl.michallysak.notes.application.quarkus.helpers;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.sse.InboundSseEvent;
import jakarta.ws.rs.sse.SseEventSource;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import lombok.Getter;

public class SseTestClient {
  @Getter private final CountDownLatch eventLatch = new CountDownLatch(1);
  @Getter private final CountDownLatch errorLatch = new CountDownLatch(1);
  private final AtomicReference<List<Throwable>> exceptions =
      new AtomicReference<>(Collections.emptyList());
  private final AtomicReference<List<InboundSseEvent>> events =
      new AtomicReference<>(Collections.emptyList());

  private final URI uri;
  private final String token;

  public SseTestClient(URI uri, String token) {
    this.uri = uri;
    this.token = token;
  }

  /**
   * Runs the SSE test with the given context. The provided BiFunction receives the SseEventSource
   * and this SseTestClient instance. The lambda should open the source, perform any actions (e.g.
   * trigger events), and return a Boolean indicating test success.
   */
  public boolean runWithContext(BiFunction<SseEventSource, SseTestClient, Boolean> onContext) {
    try (Client client = ClientBuilder.newClient()) {
      if (token != null && !token.isEmpty()) {
        ClientRequestFilter authorization =
            (requestContext) -> requestContext.getHeaders().add("Authorization", "Bearer " + token);
        client.register(authorization);
      }
      WebTarget target = client.target(uri);
      try (SseEventSource source = SseEventSource.target(target).build()) {
        source.register(this::onEventMsg, this::onError);
        return onContext.apply(source, this);
      }
    }
  }

  private void onEventMsg(InboundSseEvent event) {
    System.out.printf(
        "[SSE] Received event:\nid: %s\nname: %s\ndata: %s%n",
        event.getId(), event.getName(), event.readData());
    addValue(events, event);
    eventLatch.countDown();
  }

  private void onError(Throwable e) {
    System.err.printf("[SSE] Error occurred: %s%n", e.getMessage());
    addValue(exceptions, e);
    errorLatch.countDown();
  }

  private <T> void addValue(AtomicReference<List<T>> values, T value) {
    values.getAndUpdate(
        prev -> {
          if (prev == null || prev.isEmpty()) {
            return List.of(value);
          } else {
            var newList = new ArrayList<>(prev);
            newList.add(value);
            return newList;
          }
        });
  }

  public List<Throwable> getExceptions() {
    return exceptions.get();
  }

  public List<InboundSseEvent> getEvents() {
    return events.get();
  }
}
