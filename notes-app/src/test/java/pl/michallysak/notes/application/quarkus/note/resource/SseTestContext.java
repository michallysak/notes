package pl.michallysak.notes.application.quarkus.note.resource;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.sse.InboundSseEvent;
import jakarta.ws.rs.sse.SseEventSource;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import lombok.Getter;

public class SseTestContext {
  private final SseEventSource source;
  @Getter private final CountDownLatch eventLatch = new CountDownLatch(1);
  @Getter private final CountDownLatch errorLatch = new CountDownLatch(1);
  private final AtomicReference<List<Throwable>> exceptions =
      new AtomicReference<>(Collections.emptyList());
  private final AtomicReference<List<InboundSseEvent>> events =
      new AtomicReference<>(Collections.emptyList());

  public SseTestContext(SseEventSource source) {
    this.source = source;
    source.register(this::onEventMsg, ex -> onError(ex, errorLatch));
  }

  public void open() {
    source.open();
  }

  public void onEventMsg(InboundSseEvent event) {
    System.out.printf(
        "[SSE] Received event:\nid: %s\nname: %s\ndata: %s%n",
        event.getId(), event.getName(), event.readData());
    addValue(events, event);
    eventLatch.countDown();
  }

  public void onError(Throwable e, CountDownLatch errorLatch) {
    System.err.printf("[SSE] Error occurred: %s%n", e.getMessage());
    addValue(exceptions, e);
    if (errorLatch != null) errorLatch.countDown();
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

  public static void build(URI uri, String token, Consumer<SseTestContext> onContext) {
    try (Client client = ClientBuilder.newClient()) {
      if (token != null && !token.isEmpty()) {
        client.register(
            (ClientRequestFilter)
                (requestContext) -> {
                  requestContext.getHeaders().add("Authorization", "Bearer " + token);
                });
      }
      var target = client.target(uri);
      try (SseEventSource source = SseEventSource.target(target).build()) {
        SseTestContext ctx = new SseTestContext(source);
        onContext.accept(ctx);
      }
    }
  }

  public List<Throwable> getExceptions() {
    return exceptions.get();
  }

  public List<InboundSseEvent> getEvents() {
    return events.get();
  }

  public boolean isOpen() {
    return source != null && source.isOpen();
  }
}
