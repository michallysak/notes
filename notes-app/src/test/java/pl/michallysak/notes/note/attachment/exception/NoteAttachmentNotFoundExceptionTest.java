package pl.michallysak.notes.note.attachment.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class NoteAttachmentNotFoundExceptionTest {

  @Test
  void ofAttachmentContent_shouldSetMessage() {
    // given
    UUID attachmentId = UUID.randomUUID();
    // when
    NoteAttachmentNotFoundException exception =
        NoteAttachmentNotFoundException.ofAttachmentContent(attachmentId);
    // then
    String expected = "Attachment content not found for id: %s".formatted(attachmentId);
    assertEquals(expected, exception.getMessage());
  }
}
