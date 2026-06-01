package pl.michallysak.notes.note.attachment.domain;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import pl.michallysak.notes.note.attachment.exception.NoteAttachmentAccessException;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;

@Getter
public class NoteAttachmentContentImpl implements NoteAttachmentContent {
  private final UUID attachmentId;
  private final UUID authorId;
  private final NoteAttachmentContentValue attachmentContent;

  public NoteAttachmentContentImpl(
      UUID attachmentId, UUID authorId, NoteAttachmentContentValue attachmentContent) {
    this.attachmentId = Objects.requireNonNull(attachmentId, "Attachment id cannot be null");
    this.authorId = Objects.requireNonNull(authorId, "Author id cannot be null");
    this.attachmentContent =
        Objects.requireNonNull(attachmentContent, "Attachment content cannot be null");
  }

  @Override
  public void read(UUID actingUserId) {
    checkOwnership(actingUserId);
  }

  @Override
  public void delete(UUID actingUserId) {
    checkOwnership(actingUserId);
  }

  private void checkOwnership(UUID actingUserId) {
    if (!authorId.equals(actingUserId)) {
      throw new NoteAttachmentAccessException(attachmentId, actingUserId);
    }
  }
}
