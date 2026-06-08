package pl.michallysak.notes.note.attachment.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface NoteAttachmentMeta extends NoteAttachmentMetaActions {
  UUID getId();

  UUID getNoteId();

  UUID getAuthorId();

  String getFileName();

  String getContentType();

  long getSize();

  OffsetDateTime getCreated();
}
