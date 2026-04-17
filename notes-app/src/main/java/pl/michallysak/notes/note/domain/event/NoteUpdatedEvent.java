package pl.michallysak.notes.note.domain.event;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.model.NoteValue;

@Getter
@RequiredArgsConstructor
public class NoteUpdatedEvent implements DomainEvent<NoteValue> {
  private final UUID id;
  private final NoteValue payload;
  private final Set<UUID> recipients;

  public static NoteUpdatedEvent from(NoteValue noteValue) {
    return new NoteUpdatedEvent(
        UUID.randomUUID(), noteValue, Collections.singleton(noteValue.authorId()));
  }
}
