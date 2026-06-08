package pl.michallysak.notes.note.attachment.validator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import pl.michallysak.notes.common.exception.ValidationException;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;

class NoteAttachmentValidatorImplTest {

  private final NoteAttachmentValidatorImpl validator = new NoteAttachmentValidatorImpl();

  private CreateNoteAttachmentMeta.CreateNoteAttachmentMetaBuilder validBuilder() {
    return CreateNoteAttachmentMeta.builder()
        .noteId(UUID.randomUUID())
        .authorId(UUID.randomUUID())
        .fileName("file.txt")
        .contentType("text/plain")
        .size(10);
  }

  @Test
  void validateCreateAttachmentMeta_shouldPassForValidPayload() {
    // when
    Executable executable = () -> validator.validateCreateAttachmentMeta(validBuilder().build());
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void validateCreateAttachmentMeta_shouldThrowWhenPayloadNull() {
    // when
    Executable executable = () -> validator.validateCreateAttachmentMeta(null);
    // then
    assertThrows(ValidationException.class, executable);
  }

  @Test
  void validateCreateAttachmentMeta_shouldThrowWhenNoteIdNull() {
    // given
    CreateNoteAttachmentMeta payload = validBuilder().noteId(null).build();
    // when
    Executable executable = () -> validator.validateCreateAttachmentMeta(payload);
    // then
    assertThrows(ValidationException.class, executable);
  }

  @Test
  void validateCreateAttachmentMeta_shouldThrowWhenAuthorIdNull() {
    // given
    CreateNoteAttachmentMeta payload = validBuilder().authorId(null).build();
    // when
    Executable executable = () -> validator.validateCreateAttachmentMeta(payload);
    // then
    assertThrows(ValidationException.class, executable);
  }

  @Test
  void validateCreateAttachmentMeta_shouldThrowWhenFileNameNull() {
    // given
    CreateNoteAttachmentMeta payload = validBuilder().fileName(null).build();
    // when
    Executable executable = () -> validator.validateCreateAttachmentMeta(payload);
    // then
    assertThrows(ValidationException.class, executable);
  }

  @Test
  void validateCreateAttachmentMeta_shouldThrowWhenFileNameBlank() {
    // given
    CreateNoteAttachmentMeta payload = validBuilder().fileName("   ").build();
    // when
    Executable executable = () -> validator.validateCreateAttachmentMeta(payload);
    // then
    assertThrows(ValidationException.class, executable);
  }

  @Test
  void validateCreateAttachmentMeta_shouldThrowWhenContentTypeNull() {
    // given
    CreateNoteAttachmentMeta payload = validBuilder().contentType(null).build();
    // when
    Executable executable = () -> validator.validateCreateAttachmentMeta(payload);
    // then
    assertThrows(ValidationException.class, executable);
  }

  @Test
  void validateCreateAttachmentMeta_shouldThrowWhenContentTypeBlank() {
    // given
    CreateNoteAttachmentMeta payload = validBuilder().contentType(" ").build();
    // when
    Executable executable = () -> validator.validateCreateAttachmentMeta(payload);
    // then
    assertThrows(ValidationException.class, executable);
  }

  @Test
  void validateCreateAttachmentMeta_shouldThrowWhenSizeNegative() {
    // given
    CreateNoteAttachmentMeta payload = validBuilder().size(-1).build();
    // when
    Executable executable = () -> validator.validateCreateAttachmentMeta(payload);
    // then
    assertThrows(ValidationException.class, executable);
  }

  @Test
  void validateUploadAttachmentContentPayload_shouldPassForNonNull() {
    // given
    NoteAttachmentContentValue content = NoteAttachmentContentValue.of(new byte[] {1});
    // when
    Executable executable = () -> validator.validateUploadAttachmentContentPayload(content);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void validateUploadAttachmentContentPayload_shouldThrowWhenNull() {
    // when
    Executable executable = () -> validator.validateUploadAttachmentContentPayload(null);
    // then
    assertThrows(ValidationException.class, executable);
  }
}
