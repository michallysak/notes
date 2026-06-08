package pl.michallysak.notes.note.domain;

import java.util.Set;
import java.util.UUID;
import pl.michallysak.notes.note.model.NotePermission;
import pl.michallysak.notes.note.model.NoteShare;
import pl.michallysak.notes.note.model.NoteUpdate;

public interface NoteActions {
  void read(UUID actingUserId);

  void update(NoteUpdate noteUpdate);

  void delete(UUID actingUserId);

  void setPermissions(UUID actingUserId, UUID targetUserId, Set<NotePermission> permissions);

  void removeAccess(UUID actingUserId, UUID targetUserId);

  Set<NoteShare> getShares(UUID actingUserId);
}
