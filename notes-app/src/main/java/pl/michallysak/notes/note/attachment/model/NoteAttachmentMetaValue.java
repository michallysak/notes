package pl.michallysak.notes.note.attachment.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import pl.michallysak.notes.note.attachment.domain.NoteAttachmentMeta;

@Builder
public record NoteAttachmentMetaValue(
    UUID id,
    UUID noteId,
    UUID authorId,
    String fileName,
    String contentType,
    long size,
    OffsetDateTime created) {

  public static NoteAttachmentMetaValue from(NoteAttachmentMeta noteAttachmentMeta) {
    return NoteAttachmentMetaValue.builder()
        .id(noteAttachmentMeta.getId())
        .noteId(noteAttachmentMeta.getNoteId())
        .authorId(noteAttachmentMeta.getAuthorId())
        .fileName(noteAttachmentMeta.getFileName())
        .contentType(noteAttachmentMeta.getContentType())
        .size(noteAttachmentMeta.getSize())
        .created(noteAttachmentMeta.getCreated())
        .build();
  }
}
