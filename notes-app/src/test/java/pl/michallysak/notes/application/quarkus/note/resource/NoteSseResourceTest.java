package pl.michallysak.notes.application.quarkus.note.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.note.domain.SseDomainEventPublisher;
import pl.michallysak.notes.application.quarkus.note.dto.KeyResponse;

@ExtendWith(MockitoExtension.class)
class NoteSseResourceTest {
  @Mock SseDomainEventPublisher sseDomainEventPublisher;
  @Mock SseEventSink eventSink;
  @Mock Sse sse;

  @InjectMocks NoteSseResource noteSseResource;

  @Test
  void stream_shouldDelegateToPublisher() {
    // when
    String key = "some-key";
    noteSseResource.stream(eventSink, sse, key);
    // then
    verify(sseDomainEventPublisher).stream(eventSink, sse, key);
  }

  @Test
  void createStreamKey_shouldDelegateToPublisher() {
    // given
    Set<String> events = Set.of("NOTE_CREATED_EVENT");
    KeyResponse expectedResponse = new KeyResponse("key-123", Instant.now().plusSeconds(60));
    when(sseDomainEventPublisher.createStreamKey(events)).thenReturn(expectedResponse);
    // when
    KeyResponse result = noteSseResource.createStreamKey(events);
    // then
    assertEquals(expectedResponse, result);
    verify(sseDomainEventPublisher).createStreamKey(events);
  }
}
