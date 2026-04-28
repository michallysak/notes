package pl.michallysak.notes.note.model;

import java.util.Set;
import java.util.UUID;

public record NoteShare(UUID userId, Set<NotePermission> permissions) {

  public boolean allows(NotePermission permission) {
    return permissions.contains(permission) || permissions.contains(NotePermission.EDIT);
  }
}
