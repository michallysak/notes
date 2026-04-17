package pl.michallysak.notes.note.domain.event;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.model.NoteValue;

@Getter
@RequiredArgsConstructor
public class NoteDeletedEvent implements DomainEvent<NoteValue> {
  private final UUID id;
  private final NoteValue payload;
  private final Set<UUID> recipients;

  public static NoteDeletedEvent from(NoteValue noteValue) {
    return new NoteDeletedEvent(
        UUID.randomUUID(), noteValue, Collections.singleton(noteValue.authorId()));
  }
}
