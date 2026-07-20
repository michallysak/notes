package pl.michallysak.notes.note.domain.event;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.model.NoteShare;
import pl.michallysak.notes.note.model.NoteValue;

@Getter
@RequiredArgsConstructor
public class NotePublicShareUpsertedEvent implements DomainEvent<NoteValue> {
  private final UUID id;
  private final NoteValue payload;
  private final Set<UUID> recipients;

  public static NotePublicShareUpsertedEvent from(NoteValue noteValue) {
    Set<UUID> recipients = new HashSet<>();
    recipients.add(noteValue.authorId());
    if (noteValue.shares() != null) {
      noteValue.shares().stream().map(NoteShare::userId).forEach(recipients::add);
    }
    return new NotePublicShareUpsertedEvent(UUID.randomUUID(), noteValue, recipients);
  }
}
