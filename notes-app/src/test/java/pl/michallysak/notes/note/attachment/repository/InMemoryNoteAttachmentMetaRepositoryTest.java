package pl.michallysak.notes.note.attachment.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.note.attachment.domain.NoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.domain.NoteAttachmentMetaImpl;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.validator.NoteAttachmentValidator;

@ExtendWith(MockitoExtension.class)
class InMemoryNoteAttachmentMetaRepositoryTest {

  @Mock private NoteAttachmentValidator validator;

  private final InMemoryNoteAttachmentMetaRepository repository =
      new InMemoryNoteAttachmentMetaRepository();

  private NoteAttachmentMeta meta(UUID noteId) {
    CreateNoteAttachmentMeta create =
        CreateNoteAttachmentMeta.builder()
            .noteId(noteId)
            .authorId(UUID.randomUUID())
            .fileName("file.txt")
            .contentType("text/plain")
            .size(5)
            .build();
    return new NoteAttachmentMetaImpl(create, validator);
  }

  @Test
  void saveAndFindById_shouldReturnStoredMeta() {
    // given
    NoteAttachmentMeta meta = meta(UUID.randomUUID());
    // when
    repository.saveAttachmentMeta(meta);
    // then
    Optional<NoteAttachmentMeta> found = repository.findAttachmentMetaById(meta.getId());
    assertTrue(found.isPresent());
    assertEquals(meta, found.get());
  }

  @Test
  void findById_shouldReturnEmptyWhenMissing() {
    assertTrue(repository.findAttachmentMetaById(UUID.randomUUID()).isEmpty());
  }

  @Test
  void findByNoteId_shouldReturnOnlyMatchingNote() {
    // given
    UUID noteId = UUID.randomUUID();
    NoteAttachmentMeta first = meta(noteId);
    NoteAttachmentMeta second = meta(noteId);
    NoteAttachmentMeta other = meta(UUID.randomUUID());
    repository.saveAttachmentMeta(first);
    repository.saveAttachmentMeta(second);
    repository.saveAttachmentMeta(other);
    // when
    List<NoteAttachmentMeta> result = repository.findAttachmentMetaByNoteId(noteId);
    // then
    assertEquals(2, result.size());
    assertTrue(result.contains(first));
    assertTrue(result.contains(second));
  }

  @Test
  void findByNoteId_shouldReturnEmptyWhenNoneMatch() {
    repository.saveAttachmentMeta(meta(UUID.randomUUID()));
    assertTrue(repository.findAttachmentMetaByNoteId(UUID.randomUUID()).isEmpty());
  }

  @Test
  void delete_shouldRemoveMeta() {
    // given
    NoteAttachmentMeta meta = meta(UUID.randomUUID());
    repository.saveAttachmentMeta(meta);
    // when
    repository.deleteAttachmentMetaById(meta.getId());
    // then
    assertTrue(repository.findAttachmentMetaById(meta.getId()).isEmpty());
  }
}
