package pl.michallysak.notes.note.attachment.domain;

import java.util.UUID;

public interface NoteAttachmentActions {
  void read(UUID actingUserId);

  void delete(UUID actingUserId);
}
