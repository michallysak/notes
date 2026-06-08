package pl.michallysak.notes.note.attachment.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import pl.michallysak.notes.note.attachment.domain.NoteAttachmentMeta;

public class InMemoryNoteAttachmentMetaRepository implements NoteAttachmentMetaRepository {
  private final Map<UUID, NoteAttachmentMeta> attachmentMetas = new HashMap<>();

  @Override
  public void saveAttachmentMeta(NoteAttachmentMeta noteAttachmentMeta) {
    attachmentMetas.put(noteAttachmentMeta.getId(), noteAttachmentMeta);
  }

  @Override
  public Optional<NoteAttachmentMeta> findAttachmentMetaById(UUID attachmentId) {
    return Optional.ofNullable(attachmentMetas.get(attachmentId));
  }

  @Override
  public List<NoteAttachmentMeta> findAttachmentMetaByNoteId(UUID noteId) {
    return attachmentMetas.values().stream()
        .filter(attachment -> attachment.getNoteId().equals(noteId))
        .toList();
  }

  @Override
  public void deleteAttachmentMetaById(UUID attachmentId) {
    attachmentMetas.remove(attachmentId);
  }
}
