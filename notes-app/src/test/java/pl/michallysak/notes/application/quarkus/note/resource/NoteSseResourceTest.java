package pl.michallysak.notes.application.quarkus.note.resource;

import static org.mockito.Mockito.*;

import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.note.domain.SseDomainEventPublisher;

@ExtendWith(MockitoExtension.class)
class NoteSseResourceTest {
  @Mock SseDomainEventPublisher sseDomainEventPublisher;
  @Mock SseEventSink eventSink;
  @Mock Sse sse;

  @InjectMocks NoteSseResource noteSseResource;

  @Test
  void stream_shouldRegisterEventSink() {
    // when
    noteSseResource.stream(eventSink, sse);
    // then
    verify(sseDomainEventPublisher).register(eventSink, sse);
  }
}
