package pl.michallysak.notes.note.domain.event;

import java.util.List;
import java.util.UUID;

public interface DomainEvent<T> {
  UUID getId();

  T getPayload();

  List<UUID> getRecipients();
}
