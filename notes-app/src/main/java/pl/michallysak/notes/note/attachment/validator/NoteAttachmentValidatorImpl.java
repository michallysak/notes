package pl.michallysak.notes.note.attachment.validator;

import java.util.Set;
import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.common.validator.CommonValidator;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;

public class NoteAttachmentValidatorImpl implements NoteAttachmentValidator {
  private final CommonValidator commonValidator = new CommonValidator();

  private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

  private static final Set<String> ALLOWED_CONTENT_TYPES =
      Set.of(
          "image/jpeg",
          "image/png",
          "image/webp",
          "image/gif",
          "image/bmp",
          "image/tiff",
          "image/avif",
          "application/pdf");

  @Override
  public void validateCreateAttachmentMeta(CreateNoteAttachmentMeta createAttachmentMeta) {
    commonValidator.throwOnNull(createAttachmentMeta, "Attachment create payload cannot be null");
    commonValidator.throwOnNull(createAttachmentMeta.noteId(), "Attachment note id cannot be null");
    commonValidator.throwOnNull(
        createAttachmentMeta.authorId(), "Attachment author id cannot be null");

    validateFileName(createAttachmentMeta.fileName());
    validateContentType(createAttachmentMeta.contentType());
    validateSize(createAttachmentMeta.size());
  }

  @Override
  public void validateUploadAttachmentContentPayload(NoteAttachmentContentValue attachmentContent) {
    commonValidator.throwOnNull(attachmentContent, "Attachment content cannot be null");
  }

  private void validateFileName(String fileName) {
    if (fileName == null || fileName.isBlank()) {
      throw new ValidationException("Attachment file name cannot be blank");
    }

    String trimmed = fileName.trim();

    if (trimmed.length() > 255) {
      throw new ValidationException("Attachment file name is too long (max 255 characters)");
    }

    if (trimmed.contains("..") || trimmed.contains("/") || trimmed.contains("\\")) {
      throw new ValidationException("Attachment file name contains illegal path characters");
    }
  }

  private void validateContentType(String contentType) {
    if (contentType == null || contentType.isBlank()) {
      throw new ValidationException("Attachment content type cannot be blank");
    }

    String normalized = contentType.trim().toLowerCase();

    if (!ALLOWED_CONTENT_TYPES.contains(normalized)) {
      throw new ValidationException("Unsupported attachment content type: " + contentType);
    }
  }

  private void validateSize(long size) {
    if (size < 0) {
      throw new ValidationException("Attachment size cannot be negative");
    }

    if (size > MAX_FILE_SIZE_BYTES) {
      throw new ValidationException("Attachment size exceeds maximum allowed limit");
    }
  }
}
