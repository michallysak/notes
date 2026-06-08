package pl.michallysak.notes.note.attachment.exception;

import java.util.UUID;

public class NoteAttachmentAccessException extends RuntimeException {
  public NoteAttachmentAccessException(UUID attachmentId, UUID actingUserId) {
    super("User %s is not the author of attachment %s".formatted(actingUserId, attachmentId));
  }
}
