package pl.michallysak.notes.note.attachment.validator;

import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;

public interface NoteAttachmentValidator {
  void validateCreateAttachmentMeta(CreateNoteAttachmentMeta createAttachmentMeta);

  void validateUploadAttachmentContentPayload(NoteAttachmentContentValue attachmentContent);
}
