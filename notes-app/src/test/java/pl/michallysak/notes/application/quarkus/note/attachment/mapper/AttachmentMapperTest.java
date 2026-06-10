package pl.michallysak.notes.application.quarkus.note.attachment.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.AttachmentResponse;
import pl.michallysak.notes.application.quarkus.note.attachment.dto.CreateAttachmentMultipartForm;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentMetaValue;

class AttachmentMapperTest {

  private final AttachmentMapper attachmentMapper = new AttachmentMapperImpl();

  @Test
  void mapToResponse_shouldReturnNull_whenMetaNull() {
    // given
    NoteAttachmentMetaValue meta = null;
    // when
    AttachmentResponse response = attachmentMapper.mapToResponse(meta);
    // then
    assertNull(response);
  }

  @Test
  void mapToResponse_shouldMapAllFields() {
    // given
    NoteAttachmentMetaValue meta = createNoteAttachmentMetaValue();
    // when
    AttachmentResponse response = attachmentMapper.mapToResponse(meta);
    // then
    assertNotNull(response);
    assertEquals(meta.id(), response.getId());
    assertEquals(meta.noteId(), response.getNoteId());
    assertEquals(meta.authorId(), response.getAuthorId());
    assertEquals(meta.fileName(), response.getFileName());
    assertEquals(meta.contentType(), response.getContentType());
    assertEquals(meta.size(), response.getSize());
    assertEquals(meta.created(), response.getCreated());
  }

  @Test
  void mapToCreateMeta_shouldReturnNull_whenFormNull() {
    // when
    CreateNoteAttachmentMeta result = attachmentMapper.mapToCreateMeta(null, UUID.randomUUID());
    // then
    assertNull(result);
  }

  @Test
  void mapToCreateMeta_shouldMapFields_whenProvided() {
    // given
    UUID author = UUID.randomUUID();
    UUID noteId = UUID.randomUUID();
    byte[] content = "hello".getBytes();
    CreateAttachmentMultipartForm form = createCreateAttachmentMultipartForm(noteId, content);
    // when
    CreateNoteAttachmentMeta meta = attachmentMapper.mapToCreateMeta(form, author);
    // then
    assertNotNull(meta);
    assertEquals(author, meta.authorId());
    assertEquals(noteId, meta.noteId());
    assertEquals("file.txt", meta.fileName());
    assertEquals("text/plain", meta.contentType());
    assertEquals(content.length, meta.size());
  }

  @Test
  void mapToCreateMeta_shouldHandleNullFileAndNoteId() {
    // given
    UUID author = UUID.randomUUID();
    CreateAttachmentMultipartForm form = createCreateAttachmentMultipartForm(null, null);
    // when
    CreateNoteAttachmentMeta meta = attachmentMapper.mapToCreateMeta(form, author);
    // then
    assertNotNull(meta);
    assertEquals(author, meta.authorId());
    assertNull(meta.noteId());
    assertEquals(0L, meta.size());
  }

  private NoteAttachmentMetaValue createNoteAttachmentMetaValue() {
    return NoteAttachmentMetaValue.builder()
        .id(UUID.randomUUID())
        .noteId(UUID.randomUUID())
        .authorId(UUID.randomUUID())
        .fileName("file.txt")
        .contentType("text/plain")
        .size(123L)
        .created(OffsetDateTime.now())
        .build();
  }

  private CreateAttachmentMultipartForm createCreateAttachmentMultipartForm(
      UUID noteId, byte[] file) {
    return CreateAttachmentMultipartForm.builder()
        .noteId(noteId == null ? null : noteId.toString())
        .fileName("file.txt")
        .contentType("text/plain")
        .file(file)
        .build();
  }
}
