package pl.michallysak.notes.note.attachment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pl.michallysak.notes.note.attachment.domain.NoteAttachmentMeta;

public interface NoteAttachmentMetaRepository {
  void saveAttachmentMeta(NoteAttachmentMeta noteAttachmentMeta);

  Optional<NoteAttachmentMeta> findAttachmentMetaById(UUID attachmentId);

  List<NoteAttachmentMeta> findAttachmentMetaByNoteId(UUID noteId);

  void deleteAttachmentMetaById(UUID attachmentId);
}
