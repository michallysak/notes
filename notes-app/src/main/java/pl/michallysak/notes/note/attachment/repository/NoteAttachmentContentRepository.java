package pl.michallysak.notes.note.attachment.repository;

import java.util.Optional;
import java.util.UUID;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;

public interface NoteAttachmentContentRepository {
  void saveAttachmentContent(UUID attachmentId, NoteAttachmentContentValue attachmentContent);

  Optional<NoteAttachmentContentValue> findAttachmentContentByAttachmentId(UUID attachmentId);

  void deleteAttachmentContentByAttachmentId(UUID attachmentId);
}
