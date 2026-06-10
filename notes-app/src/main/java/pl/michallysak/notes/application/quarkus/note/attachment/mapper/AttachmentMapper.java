package pl.michallysak.notes.application.quarkus.note.attachment.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.AttachmentResponse;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.CreateAttachmentMultipartForm;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentMetaValue;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI)
@ApplicationScoped
public abstract class AttachmentMapper {

  public abstract AttachmentResponse mapToResponse(NoteAttachmentMetaValue meta);

  public CreateNoteAttachmentMeta mapToCreateMeta(
      CreateAttachmentMultipartForm form, UUID authorId) {
    if (form == null) {
      return null;
    }
    byte[] content = form.getFile();
    UUID noteId = form.getNoteId() == null ? null : UUID.fromString(form.getNoteId());
    String fileName = form.getFileName();
    String contentType = form.getContentType();
    long size = content == null ? 0L : content.length;

    return CreateNoteAttachmentMeta.builder()
        .noteId(noteId)
        .authorId(authorId)
        .fileName(fileName)
        .contentType(contentType)
        .size(size)
        .build();
  }
}
