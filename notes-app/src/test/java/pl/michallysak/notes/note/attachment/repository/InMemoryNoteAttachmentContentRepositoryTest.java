package pl.michallysak.notes.note.attachment.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;

class InMemoryNoteAttachmentContentRepositoryTest {

  private final InMemoryNoteAttachmentContentRepository repository =
      new InMemoryNoteAttachmentContentRepository();

  @Test
  void saveAndFind_shouldReturnStoredContent() {
    // given
    UUID attachmentId = UUID.randomUUID();
    NoteAttachmentContentValue content = NoteAttachmentContentValue.of(new byte[] {1, 2, 3});
    // when
    repository.saveAttachmentContent(attachmentId, content);
    // then
    Optional<NoteAttachmentContentValue> found =
        repository.findAttachmentContentByAttachmentId(attachmentId);
    assertTrue(found.isPresent());
    assertEquals(content, found.get());
  }

  @Test
  void find_shouldReturnEmptyWhenMissing() {
    assertTrue(repository.findAttachmentContentByAttachmentId(UUID.randomUUID()).isEmpty());
  }

  @Test
  void save_shouldOverwriteExistingContent() {
    // given
    UUID attachmentId = UUID.randomUUID();
    repository.saveAttachmentContent(attachmentId, NoteAttachmentContentValue.of(new byte[] {1}));
    NoteAttachmentContentValue updated = NoteAttachmentContentValue.of(new byte[] {9, 9});
    // when
    repository.saveAttachmentContent(attachmentId, updated);
    // then
    assertEquals(
        updated, repository.findAttachmentContentByAttachmentId(attachmentId).orElseThrow());
  }

  @Test
  void delete_shouldRemoveContent() {
    // given
    UUID attachmentId = UUID.randomUUID();
    repository.saveAttachmentContent(attachmentId, NoteAttachmentContentValue.of(new byte[] {1}));
    // when
    repository.deleteAttachmentContentByAttachmentId(attachmentId);
    // then
    assertTrue(repository.findAttachmentContentByAttachmentId(attachmentId).isEmpty());
  }

  @Test
  void delete_shouldBeNoOpWhenMissing() {
    repository.deleteAttachmentContentByAttachmentId(UUID.randomUUID());
    assertTrue(repository.findAttachmentContentByAttachmentId(UUID.randomUUID()).isEmpty());
  }
}
