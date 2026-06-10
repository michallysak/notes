package pl.michallysak.notes.application.quarkus.note.attachment.controller;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.AttachmentResponse;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.CreateAttachmentMultipartForm;
import pl.michallysak.notes.application.quarkus.note.attachment.mapper.AttachmentMapper;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentMetaValue;
import pl.michallysak.notes.note.attachment.service.NoteAttachmentService;
import pl.michallysak.notes.user.service.CurrentUserProvider;

@ApplicationScoped
@RequiredArgsConstructor
public class NoteAttachmentController {
  private final NoteAttachmentService noteAttachmentService;
  private final CurrentUserProvider currentUserProvider;
  private final AttachmentMapper attachmentMapper;
  // FIXME move to custom domain exception and throw on service level
  private final Supplier<IllegalArgumentException> attachmentNotFound =
      () -> new IllegalArgumentException("Attachment not found");

  public AttachmentResponse createAttachment(CreateAttachmentMultipartForm form) {
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    CreateNoteAttachmentMeta createMeta = attachmentMapper.mapToCreateMeta(form, currentUserId);
    NoteAttachmentMetaValue metaValue = noteAttachmentService.createAttachmentMeta(createMeta);
    NoteAttachmentContentValue attachmentContent = NoteAttachmentContentValue.of(form.getFile());
    noteAttachmentService.uploadAttachmentContent(metaValue.id(), currentUserId, attachmentContent);
    return attachmentMapper.mapToResponse(metaValue);
  }

  public AttachmentResponse getAttachment(UUID id) {
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    NoteAttachmentMetaValue metaValue =
        noteAttachmentService.getAttachmentMeta(id, currentUserId).orElseThrow(attachmentNotFound);
    return attachmentMapper.mapToResponse(metaValue);
  }

  public List<AttachmentResponse> getAttachmentsForNote(UUID noteId) {
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    return noteAttachmentService.getAttachmentMetasForNote(noteId, currentUserId).stream()
        .map(attachmentMapper::mapToResponse)
        .toList();
  }

  public void deleteAttachment(UUID id) {
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    noteAttachmentService.deleteAttachmentMeta(id, currentUserId);
  }

  public AttachmentContent downloadAttachmentContent(UUID id) {
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    NoteAttachmentMetaValue meta =
        noteAttachmentService.getAttachmentMeta(id, currentUserId).orElseThrow(attachmentNotFound);
    NoteAttachmentContentValue content =
        noteAttachmentService.downloadAttachmentContent(id, currentUserId);
    return new AttachmentContent(content.value(), meta.contentType(), meta.fileName());
  }
}
