package pl.michallysak.notes.note.attachment.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.attachment.domain.NoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.domain.NoteAttachmentContentImpl;
import pl.michallysak.notes.note.attachment.domain.NoteAttachmentMetaMetaImpl;
import pl.michallysak.notes.note.attachment.exception.NoteAttachmentNotFoundException;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentMetaValue;
import pl.michallysak.notes.note.attachment.repository.NoteAttachmentContentRepository;
import pl.michallysak.notes.note.attachment.repository.NoteAttachmentMetaRepository;
import pl.michallysak.notes.note.attachment.validator.NoteAttachmentValidator;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.exception.NoteNotFoundException;
import pl.michallysak.notes.note.repository.NoteRepository;

@RequiredArgsConstructor
public class NoteAttachmentServiceImpl implements NoteAttachmentService {
  private final NoteAttachmentMetaRepository attachmentMetaRepository;
  private final NoteAttachmentContentRepository attachmentContentRepository;
  private final NoteRepository noteRepository;
  private final NoteAttachmentValidator noteAttachmentValidator;

  @Override
  public NoteAttachmentMetaValue createAttachmentMeta(
      CreateNoteAttachmentMeta createAttachmentMeta) {
    Note note = findNoteByIdOrThrow(createAttachmentMeta.noteId());
    NoteAttachmentMeta noteAttachmentMeta =
        new NoteAttachmentMetaMetaImpl(createAttachmentMeta, noteAttachmentValidator);
    note.addAttachment(createAttachmentMeta.authorId(), noteAttachmentMeta.getId());
    noteRepository.saveNote(note);
    attachmentMetaRepository.saveAttachmentMeta(noteAttachmentMeta);
    return NoteAttachmentMetaValue.from(noteAttachmentMeta);
  }

  @Override
  public Optional<NoteAttachmentMetaValue> getAttachmentMeta(UUID attachmentId) {
    validateAttachmentId(attachmentId);
    return attachmentMetaRepository
        .findAttachmentMetaById(attachmentId)
        .map(NoteAttachmentMetaValue::from);
  }

  @Override
  public List<NoteAttachmentMetaValue> getAttachmentMetasForNote(UUID noteId) {
    Objects.requireNonNull(noteId, "Note id cannot be null");
    return attachmentMetaRepository.findAttachmentMetaByNoteId(noteId).stream()
        .map(NoteAttachmentMetaValue::from)
        .toList();
  }

  @Override
  public void deleteAttachmentMeta(UUID attachmentId, UUID actingUserId) {
    validateAttachmentId(attachmentId);
    NoteAttachmentMeta noteAttachmentMeta =
        attachmentMetaRepository
            .findAttachmentMetaById(attachmentId)
            .orElseThrow(
                () -> NoteAttachmentNotFoundException.ofAttachmentMeta(attachmentId));
    Note note = findNoteByIdOrThrow(noteAttachmentMeta.getNoteId());
    noteAttachmentMeta.delete(actingUserId);
    note.deleteAttachment(actingUserId, attachmentId);
    noteRepository.saveNote(note);
    attachmentMetaRepository.deleteAttachmentMetaById(attachmentId);
  }

  @Override
  public void uploadAttachmentContent(UUID attachmentId, NoteAttachmentContentValue attachmentContent) {
    validateAttachmentId(attachmentId);
    noteAttachmentValidator.validateUploadAttachmentContentPayload(attachmentContent);
    NoteAttachmentMeta noteAttachmentMeta = findAttachmentMetaByIdOrThrow(attachmentId);
    NoteAttachmentContentImpl content =
        new NoteAttachmentContentImpl(
            attachmentId, noteAttachmentMeta.getAuthorId(), attachmentContent);
    attachmentContentRepository.saveAttachmentContent(
        content.getAttachmentId(), content.getAttachmentContent());
  }

  @Override
  public NoteAttachmentContentValue downloadAttachmentContent(UUID attachmentId) {
    validateAttachmentId(attachmentId);
    NoteAttachmentMeta noteAttachmentMeta = findAttachmentMetaByIdOrThrow(attachmentId);
    NoteAttachmentContentValue attachmentContent =
        attachmentContentRepository
            .findAttachmentContentByAttachmentId(attachmentId)
            .orElseThrow(() -> NoteAttachmentNotFoundException.ofAttachmentContent(attachmentId));
    NoteAttachmentContentImpl content =
        new NoteAttachmentContentImpl(
            attachmentId, noteAttachmentMeta.getAuthorId(), attachmentContent);
    return content.getAttachmentContent();
  }

  @Override
  public void deleteAttachmentContent(UUID attachmentId) {
    validateAttachmentId(attachmentId);
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

  private Note findNoteByIdOrThrow(UUID noteId) {
    return noteRepository.findNoteWithId(noteId).orElseThrow(NoteNotFoundException::new);
  }

}
