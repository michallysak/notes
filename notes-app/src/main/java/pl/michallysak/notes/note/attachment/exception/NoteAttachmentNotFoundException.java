package pl.michallysak.notes.note.attachment.exception;

import java.util.UUID;
import pl.michallysak.notes.common.exception.EntityNotFoundException;

public class NoteAttachmentNotFoundException extends EntityNotFoundException {
  private NoteAttachmentNotFoundException(UUID attachmentId, String resourceType) {
    super("Attachment %s not found for id: %s".formatted(resourceType, attachmentId));
  }

  public static NoteAttachmentNotFoundException ofAttachmentMeta(UUID attachmentId) {
    return new NoteAttachmentNotFoundException(attachmentId, "metadata");
  }

  public static NoteAttachmentNotFoundException ofAttachmentContent(UUID attachmentId) {
    return new NoteAttachmentNotFoundException(attachmentId, "content");
  }
}
