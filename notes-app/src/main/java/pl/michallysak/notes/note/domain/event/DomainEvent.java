package pl.michallysak.notes.note.domain.event;

import java.util.Set;
import java.util.UUID;

public interface DomainEvent<T> {
  UUID getId();

  T getPayload();

  Set<UUID> getRecipients();
}
