package pl.michallysak.notes.note.domain;

import java.util.UUID;
import pl.michallysak.notes.note.model.NoteUpdate;

public interface NoteActions {
  void read(UUID actingUserId);

  void update(NoteUpdate noteUpdate);

  void delete(UUID actingUserId);
}
