package pl.michallysak.notes.note.attachment.domain;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import pl.michallysak.notes.note.attachment.exception.NoteAttachmentAccessException;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentMetaValue;
import pl.michallysak.notes.note.attachment.validator.NoteAttachmentValidator;
import pl.michallysak.notes.note.model.NotePermission;
import pl.michallysak.notes.note.model.NoteShare;

@Getter
public class NoteAttachmentMetaImpl implements NoteAttachmentMeta {
  private final UUID id;
  private final UUID noteId;
  private final UUID authorId;
  private final String fileName;
  private final String contentType;
  private final long size;
  private final OffsetDateTime created;
  private final NoteAttachmentValidator noteAttachmentValidator;

  public NoteAttachmentMetaImpl(
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

  public NoteAttachmentMetaImpl(
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
  public void read(UUID actingUserId, Set<NoteShare> shares) {
    checkPermission(actingUserId, shares, NotePermission.READ);
  }

  @Override
  public void delete(UUID actingUserId, Set<NoteShare> shares) {
    checkPermission(actingUserId, shares, NotePermission.EDIT);
  }

  @Override
  public void uploadContent(UUID actingUserId, Set<NoteShare> shares) {
    checkPermission(actingUserId, shares, NotePermission.EDIT);
  }

  @Override
  public void downloadContent(UUID actingUserId, Set<NoteShare> shares) {
    checkPermission(actingUserId, shares, NotePermission.READ);
  }

  @Override
  public void deleteContent(UUID actingUserId, Set<NoteShare> shares) {
    checkPermission(actingUserId, shares, NotePermission.EDIT);
  }

  private void checkPermission(UUID actingUserId, Set<NoteShare> shares, NotePermission required) {
    if (authorId.equals(actingUserId)) {
      return;
    }

    Optional<NoteShare> userShare =
        shares.stream().filter(s -> s.userId().equals(actingUserId)).findFirst();

    if (userShare.isPresent() && userShare.get().allows(required)) {
      return;
    }

    throw new NoteAttachmentAccessException(id, actingUserId);
  }
}
