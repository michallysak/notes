package pl.michallysak.notes.note.attachment.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.attachment.domain.NoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.domain.NoteAttachmentMetaImpl;
import pl.michallysak.notes.note.attachment.exception.NoteAttachmentNotFoundException;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentMetaValue;
import pl.michallysak.notes.note.attachment.repository.NoteAttachmentContentRepository;
import pl.michallysak.notes.note.attachment.repository.NoteAttachmentMetaRepository;
import pl.michallysak.notes.note.attachment.validator.NoteAttachmentValidator;

@RequiredArgsConstructor
public class NoteAttachmentServiceImpl implements NoteAttachmentService {
  private final NoteAttachmentMetaRepository attachmentMetaRepository;
  private final NoteAttachmentContentRepository attachmentContentRepository;
  private final NoteAttachmentValidator noteAttachmentValidator;

  @Override
  public NoteAttachmentMetaValue createAttachmentMeta(
      CreateNoteAttachmentMeta createAttachmentMeta) {
    NoteAttachmentMeta noteAttachmentMeta =
        new NoteAttachmentMetaImpl(createAttachmentMeta, noteAttachmentValidator);
    attachmentMetaRepository.saveAttachmentMeta(noteAttachmentMeta);
    return NoteAttachmentMetaValue.from(noteAttachmentMeta);
  }

  @Override
  public Optional<NoteAttachmentMetaValue> getAttachmentMeta(UUID attachmentId, UUID actingUserId) {
    validateAttachmentId(attachmentId);
    return attachmentMetaRepository
        .findAttachmentMetaById(attachmentId)
        .map(
            meta -> {
              meta.read(actingUserId);
              return NoteAttachmentMetaValue.from(meta);
            });
  }

  @Override
  public List<NoteAttachmentMetaValue> getAttachmentMetasForNote(UUID noteId, UUID actingUserId) {
    Objects.requireNonNull(noteId, "Note id cannot be null");
    return attachmentMetaRepository.findAttachmentMetaByNoteId(noteId).stream()
        .peek(meta -> meta.read(actingUserId))
        .map(NoteAttachmentMetaValue::from)
        .toList();
  }

  @Override
  public void deleteAttachmentMeta(UUID attachmentId, UUID actingUserId) {
    validateAttachmentId(attachmentId);
    NoteAttachmentMeta noteAttachmentMeta = findAttachmentMetaByIdOrThrow(attachmentId);
    noteAttachmentMeta.delete(actingUserId);

    attachmentMetaRepository.deleteAttachmentMetaById(attachmentId);
    attachmentContentRepository.deleteAttachmentContentByAttachmentId(attachmentId);
  }

  @Override
  public void uploadAttachmentContent(
      UUID attachmentId, UUID actingUserId, NoteAttachmentContentValue attachmentContent) {
    validateAttachmentId(attachmentId);
    noteAttachmentValidator.validateUploadAttachmentContentPayload(attachmentContent);
    NoteAttachmentMeta noteAttachmentMeta = findAttachmentMetaByIdOrThrow(attachmentId);
    noteAttachmentMeta.uploadContent(actingUserId);

    attachmentContentRepository.saveAttachmentContent(attachmentId, attachmentContent);
  }

  @Override
  public NoteAttachmentContentValue downloadAttachmentContent(
      UUID attachmentId, UUID actingUserId) {
    validateAttachmentId(attachmentId);
    NoteAttachmentMeta noteAttachmentMeta = findAttachmentMetaByIdOrThrow(attachmentId);
    noteAttachmentMeta.downloadContent(actingUserId);

    return attachmentContentRepository
        .findAttachmentContentByAttachmentId(attachmentId)
        .orElseThrow(() -> NoteAttachmentNotFoundException.ofAttachmentContent(attachmentId));
  }

  @Override
  public void deleteAttachmentContent(UUID attachmentId, UUID actingUserId) {
    validateAttachmentId(attachmentId);
    NoteAttachmentMeta noteAttachmentMeta = findAttachmentMetaByIdOrThrow(attachmentId);
    noteAttachmentMeta.deleteContent(actingUserId);

    attachmentContentRepository.deleteAttachmentContentByAttachmentId(attachmentId);
  }

  private void validateAttachmentId(UUID attachmentId) {
    Objects.requireNonNull(attachmentId, "Attachment id cannot be null");
  }

  private NoteAttachmentMeta findAttachmentMetaByIdOrThrow(UUID attachmentId) {
    return attachmentMetaRepository
        .findAttachmentMetaById(attachmentId)
        .orElseThrow(() -> NoteAttachmentNotFoundException.ofAttachmentMeta(attachmentId));
  }
}
