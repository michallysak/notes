package pl.michallysak.notes.note.domain.event;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.model.NoteAndPublicShareId;
import pl.michallysak.notes.note.model.NoteShare;
import pl.michallysak.notes.note.model.NoteValue;

@Getter
@RequiredArgsConstructor
public class NotePublicShareRemovedEvent implements DomainEvent<NoteAndPublicShareId> {
  private final UUID id;
  private final NoteAndPublicShareId payload;
  private final Set<UUID> recipients;

  public static NotePublicShareRemovedEvent from(NoteValue noteValue, UUID publicShareId) {
    NoteAndPublicShareId payload = new NoteAndPublicShareId(noteValue.id(), publicShareId);
    Set<UUID> recipients = new HashSet<>();
    recipients.add(noteValue.authorId());
    if (noteValue.shares() != null) {
      noteValue.shares().stream().map(NoteShare::userId).forEach(recipients::add);
    }
    return new NotePublicShareRemovedEvent(UUID.randomUUID(), payload, recipients);
  }
}
