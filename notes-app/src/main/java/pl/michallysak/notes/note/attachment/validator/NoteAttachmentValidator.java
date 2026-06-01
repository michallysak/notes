package pl.michallysak.notes.note.attachment.validator;

import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;

public interface NoteAttachmentValidator {
  void validateCreateAttachmentMeta(CreateNoteAttachmentMeta createAttachmentMeta);

  void validateUploadAttachmentContentPayload(NoteAttachmentContentValue attachmentContent);
}
