package pl.michallysak.notes.application.quarkus.note.domain;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.domain.event.DomainEvent;

@Getter
@RequiredArgsConstructor
class TestDomainEvent<T> implements DomainEvent<T> {
  private final UUID id;
  private final T payload;
  private final List<UUID> recipients;
}
