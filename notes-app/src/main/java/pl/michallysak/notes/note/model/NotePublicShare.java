package pl.michallysak.notes.note.model;

import java.util.Set;
import java.util.UUID;

public record NotePublicShare(UUID publicShareId, Set<NotePermission> permissions) {

  public boolean allows(NotePermission permission) {
    return permissions.contains(permission) || permissions.contains(NotePermission.EDIT);
  }
}
