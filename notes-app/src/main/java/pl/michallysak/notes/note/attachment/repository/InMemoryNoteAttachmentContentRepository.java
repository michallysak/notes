package pl.michallysak.notes.note.attachment.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;

public class InMemoryNoteAttachmentContentRepository implements NoteAttachmentContentRepository {
  private final Map<UUID, NoteAttachmentContentValue> attachmentContents = new HashMap<>();

  @Override
  public void saveAttachmentContent(
      UUID attachmentId, NoteAttachmentContentValue attachmentContent) {
    attachmentContents.put(attachmentId, attachmentContent);
  }

  @Override
  public Optional<NoteAttachmentContentValue> findAttachmentContentByAttachmentId(
      UUID attachmentId) {
    return Optional.ofNullable(attachmentContents.get(attachmentId));
  }

  @Override
  public void deleteAttachmentContentByAttachmentId(UUID attachmentId) {
    attachmentContents.remove(attachmentId);
  }
}
