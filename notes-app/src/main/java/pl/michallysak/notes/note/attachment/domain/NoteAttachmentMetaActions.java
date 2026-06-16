package pl.michallysak.notes.note.attachment.domain;

import java.util.Set;
import java.util.UUID;
import pl.michallysak.notes.note.model.NoteShare;

public interface NoteAttachmentMetaActions {
  void read(UUID actingUserId, Set<NoteShare> shares);

  void delete(UUID actingUserId, Set<NoteShare> shares);

  void uploadContent(UUID actingUserId, Set<NoteShare> shares);

  void downloadContent(UUID actingUserId, Set<NoteShare> shares);

  void deleteContent(UUID actingUserId, Set<NoteShare> shares);
}
