package pl.michallysak.notes.note.attachment.domain;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import pl.michallysak.notes.note.attachment.exception.NoteAttachmentAccessException;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentMetaValue;
import pl.michallysak.notes.note.attachment.validator.NoteAttachmentValidator;

@Getter
public class NoteAttachmentMetaMetaImpl implements NoteAttachmentMeta {
  private final UUID id;
  private final UUID noteId;
  private final UUID authorId;
  private final String fileName;
  private final String contentType;
  private final long size;
  private final OffsetDateTime created;
  private final NoteAttachmentValidator noteAttachmentValidator;

  public NoteAttachmentMetaMetaImpl(
      CreateNoteAttachmentMeta createMeta, NoteAttachmentValidator noteAttachmentValidator) {
    this.noteAttachmentValidator = noteAttachmentValidator;
    noteAttachmentValidator.validateCreateAttachmentMeta(createMeta);
    this.id = UUID.randomUUID();
    this.noteId = createMeta.noteId();
    this.authorId = createMeta.authorId();
    this.fileName = createMeta.fileName();
    this.contentType = createMeta.contentType();
    this.size = createMeta.size();
    this.created = OffsetDateTime.now();
  }

  public NoteAttachmentMetaMetaImpl(
      NoteAttachmentMetaValue value, NoteAttachmentValidator noteAttachmentValidator) {
    this.noteAttachmentValidator = noteAttachmentValidator;
    this.id = value.id();
    this.noteId = value.noteId();
    this.authorId = value.authorId();
    this.fileName = value.fileName();
    this.contentType = value.contentType();
    this.size = value.size();
    this.created = value.created();
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
      throw new NoteAttachmentAccessException(id, actingUserId);
    }
  }
}
