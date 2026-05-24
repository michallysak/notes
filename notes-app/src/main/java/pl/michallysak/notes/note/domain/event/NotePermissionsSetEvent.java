package pl.michallysak.notes.note.domain.event;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.model.NotePermission;

@Getter
@RequiredArgsConstructor
public class NotePermissionsSetEvent implements DomainEvent<NotePermissionEventPayload> {
  private final UUID id;
  private final NotePermissionEventPayload payload;
  private final Set<UUID> recipients;

  public static NotePermissionsSetEvent from(
      UUID noteId, UUID actingUserId, UUID targetUserId, Set<NotePermission> permissions) {
    NotePermissionEventPayload payload =
        new NotePermissionEventPayload(noteId, targetUserId, permissions);
    Set<UUID> recipients = new HashSet<>();
    recipients.add(actingUserId);
    recipients.add(targetUserId);
    return new NotePermissionsSetEvent(UUID.randomUUID(), payload, recipients);
  }
}
