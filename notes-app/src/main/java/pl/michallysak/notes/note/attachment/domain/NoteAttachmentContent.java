package pl.michallysak.notes.note.attachment.domain;

import java.util.UUID;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;

public interface NoteAttachmentContent extends NoteAttachmentActions {
  UUID getAttachmentId();

  NoteAttachmentContentValue getAttachmentContent();
}
