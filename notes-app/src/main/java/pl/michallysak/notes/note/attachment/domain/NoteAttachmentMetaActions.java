package pl.michallysak.notes.note.attachment.domain;

import java.util.UUID;

public interface NoteAttachmentMetaActions {
  void read(UUID actingUserId);

  void delete(UUID actingUserId);

  void uploadContent(UUID actingUserId);

  void downloadContent(UUID actingUserId);

  void deleteContent(UUID actingUserId);
}
