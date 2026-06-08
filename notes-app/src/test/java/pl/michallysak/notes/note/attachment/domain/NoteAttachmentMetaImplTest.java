package pl.michallysak.notes.note.attachment.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.note.attachment.exception.NoteAttachmentAccessException;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentMetaValue;
import pl.michallysak.notes.note.attachment.validator.NoteAttachmentValidator;

@ExtendWith(MockitoExtension.class)
class NoteAttachmentMetaImplTest {

  @Mock private NoteAttachmentValidator validator;

  private CreateNoteAttachmentMeta createMeta(UUID authorId) {
    return CreateNoteAttachmentMeta.builder()
        .noteId(UUID.randomUUID())
        .authorId(authorId)
        .fileName("file.txt")
        .contentType("text/plain")
        .size(10)
        .build();
  }

  @Test
  void constructor_shouldInitializeFieldsAndValidate() {
    // given
    UUID authorId = UUID.randomUUID();
    CreateNoteAttachmentMeta create = createMeta(authorId);
    // when
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(create, validator);
    // then
    assertNotNull(meta.getId());
    assertEquals(authorId, meta.getAuthorId());
    assertEquals(create.noteId(), meta.getNoteId());
    assertEquals("file.txt", meta.getFileName());
    assertEquals("text/plain", meta.getContentType());
    assertEquals(10, meta.getSize());
    assertNotNull(meta.getCreated());
  }

  @Test
  void valueConstructor_shouldRestoreFields() {
    // given
    UUID id = UUID.randomUUID();
    UUID noteId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    OffsetDateTime created = OffsetDateTime.now();
    NoteAttachmentMetaValue value =
        NoteAttachmentMetaValue.builder()
            .id(id)
            .noteId(noteId)
            .authorId(authorId)
            .fileName("doc.pdf")
            .contentType("application/pdf")
            .size(42)
            .created(created)
            .build();
    // when
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(value, validator);
    // then
    assertEquals(id, meta.getId());
    assertEquals(noteId, meta.getNoteId());
    assertEquals(authorId, meta.getAuthorId());
    assertEquals("doc.pdf", meta.getFileName());
    assertEquals("application/pdf", meta.getContentType());
    assertEquals(42, meta.getSize());
    assertEquals(created, meta.getCreated());
  }

  @Test
  void actions_shouldAllowOwner() {
    // given
    UUID authorId = UUID.randomUUID();
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(authorId), validator);
    // when
    Executable executable =
        () -> {
          meta.read(authorId);
          meta.delete(authorId);
          meta.uploadContent(authorId);
          meta.downloadContent(authorId);
          meta.deleteContent(authorId);
        };
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void actions_shouldRejectNonOwner() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID intruder = UUID.randomUUID();
    // when & then
    assertThrows(NoteAttachmentAccessException.class, () -> meta.read(intruder));
    assertThrows(NoteAttachmentAccessException.class, () -> meta.delete(intruder));
    assertThrows(NoteAttachmentAccessException.class, () -> meta.uploadContent(intruder));
    assertThrows(NoteAttachmentAccessException.class, () -> meta.downloadContent(intruder));
    assertThrows(NoteAttachmentAccessException.class, () -> meta.deleteContent(intruder));
  }
}
