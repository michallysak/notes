package pl.michallysak.notes.note.domain.event;

import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import pl.michallysak.notes.note.model.NoteAndUserId;
import pl.michallysak.notes.note.model.NotePermission;

@Getter
public class NotePermissionEventPayload extends NoteAndUserId {
  private final Set<NotePermission> permissions;

  public NotePermissionEventPayload(UUID noteId, UUID userId, Set<NotePermission> permissions) {
    super(noteId, userId);
    this.permissions = permissions;
  }
}
