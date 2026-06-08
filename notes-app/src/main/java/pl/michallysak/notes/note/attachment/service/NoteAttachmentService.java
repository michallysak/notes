package pl.michallysak.notes.note.attachment.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentMetaValue;

public interface NoteAttachmentService {
  NoteAttachmentMetaValue createAttachmentMeta(CreateNoteAttachmentMeta createAttachmentMeta);

  Optional<NoteAttachmentMetaValue> getAttachmentMeta(UUID attachmentId, UUID actingUserId);

  List<NoteAttachmentMetaValue> getAttachmentMetasForNote(UUID noteId, UUID actingUserId);

  void deleteAttachmentMeta(UUID attachmentId, UUID actingUserId);

  void uploadAttachmentContent(
      UUID attachmentId, UUID actingUserId, NoteAttachmentContentValue attachmentContent);

  NoteAttachmentContentValue downloadAttachmentContent(UUID attachmentId, UUID actingUserId);

  void deleteAttachmentContent(UUID attachmentId, UUID actingUserId);
}
