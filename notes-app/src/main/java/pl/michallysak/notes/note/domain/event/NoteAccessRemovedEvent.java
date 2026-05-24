package pl.michallysak.notes.note.domain.event;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.model.NoteAndUserId;

@Getter
@RequiredArgsConstructor
public class NoteAccessRemovedEvent implements DomainEvent<NoteAndUserId> {
  private final UUID id;
  private final NoteAndUserId payload;
  private final Set<UUID> recipients;

  public static NoteAccessRemovedEvent from(UUID noteId, UUID actingUserId, UUID targetUserId) {
    NoteAndUserId payload = new NoteAndUserId(noteId, targetUserId);
    Set<UUID> recipients = new HashSet<>();
    recipients.add(actingUserId);
    recipients.add(targetUserId);
    return new NoteAccessRemovedEvent(UUID.randomUUID(), payload, recipients);
  }
}
