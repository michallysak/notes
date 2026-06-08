package pl.michallysak.notes.note.attachment.validator;

import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.common.validator.CommonValidator;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;

public class NoteAttachmentValidatorImpl implements NoteAttachmentValidator {
  private final CommonValidator commonValidator = new CommonValidator();

  @Override
  public void validateCreateAttachmentMeta(CreateNoteAttachmentMeta createAttachmentMeta) {
    commonValidator.throwOnNull(createAttachmentMeta, "Attachment create payload cannot be null");
    commonValidator.throwOnNull(createAttachmentMeta.noteId(), "Attachment note id cannot be null");
    commonValidator.throwOnNull(
        createAttachmentMeta.authorId(), "Attachment author id cannot be null");

    if (createAttachmentMeta.fileName() == null || createAttachmentMeta.fileName().isBlank()) {
      throw new ValidationException("Attachment file name cannot be blank");
    }
    if (createAttachmentMeta.contentType() == null
        || createAttachmentMeta.contentType().isBlank()) {
      throw new ValidationException("Attachment content type cannot be blank");
    }
    if (createAttachmentMeta.size() < 0) {
      throw new ValidationException("Attachment size cannot be negative");
    }
  }

  @Override
  public void validateUploadAttachmentContentPayload(NoteAttachmentContentValue attachmentContent) {
    commonValidator.throwOnNull(attachmentContent, "Attachment content cannot be null");
  }
}
