package pl.michallysak.notes.note.attachment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.note.attachment.domain.NoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.domain.NoteAttachmentMetaImpl;
import pl.michallysak.notes.note.attachment.exception.NoteAttachmentAccessException;
import pl.michallysak.notes.note.attachment.exception.NoteAttachmentNotFoundException;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentMetaValue;
import pl.michallysak.notes.note.attachment.repository.NoteAttachmentContentRepository;
import pl.michallysak.notes.note.attachment.repository.NoteAttachmentMetaRepository;
import pl.michallysak.notes.note.attachment.validator.NoteAttachmentValidator;

@ExtendWith(MockitoExtension.class)
class NoteAttachmentServiceImplTest {

  @Mock private NoteAttachmentMetaRepository metaRepository;
  @Mock private NoteAttachmentContentRepository contentRepository;
  @Mock private NoteAttachmentValidator validator;

  @InjectMocks private NoteAttachmentServiceImpl service;

  private static final UUID AUTHOR_ID = UUID.randomUUID();

  private NoteAttachmentMeta meta() {
    CreateNoteAttachmentMeta create =
        CreateNoteAttachmentMeta.builder()
            .noteId(UUID.randomUUID())
            .authorId(AUTHOR_ID)
            .fileName("file.txt")
            .contentType("text/plain")
            .size(5)
            .build();
    return new NoteAttachmentMetaImpl(create, validator);
  }

  @Test
  void createAttachmentMeta_shouldValidateAndSave() {
    // given
    CreateNoteAttachmentMeta create =
        CreateNoteAttachmentMeta.builder()
            .noteId(UUID.randomUUID())
            .authorId(AUTHOR_ID)
            .fileName("file.txt")
            .contentType("text/plain")
            .size(5)
            .build();
    // when
    NoteAttachmentMetaValue value = service.createAttachmentMeta(create);
    // then
    verify(validator).validateCreateAttachmentMeta(create);
    verify(metaRepository).saveAttachmentMeta(any());
    assertEquals("file.txt", value.fileName());
  }

  @Test
  void getAttachmentMeta_shouldEnforceAccessAndMap() {
    // given
    NoteAttachmentMeta meta = meta();
    when(metaRepository.findAttachmentMetaById(any())).thenReturn(Optional.of(meta));
    // when
    Optional<NoteAttachmentMetaValue> result = service.getAttachmentMeta(meta.getId(), AUTHOR_ID);
    // then
    assertTrue(result.isPresent());
    assertEquals(meta.getId(), result.get().id());
  }

  @Test
  void getAttachmentMeta_shouldRejectNonOwner() {
    // given
    NoteAttachmentMeta meta = meta();
    when(metaRepository.findAttachmentMetaById(any())).thenReturn(Optional.of(meta));
    UUID intruder = UUID.randomUUID();
    // when
    Executable executable = () -> service.getAttachmentMeta(meta.getId(), intruder);
    // then
    assertThrows(NoteAttachmentAccessException.class, executable);
  }

  @Test
  void getAttachmentMetasForNote_shouldEnforceAccessPerItem() {
    // given
    NoteAttachmentMeta meta = meta();
    UUID noteId = meta.getNoteId();
    when(metaRepository.findAttachmentMetaByNoteId(eq(noteId))).thenReturn(List.of(meta));
    // when
    List<NoteAttachmentMetaValue> result = service.getAttachmentMetasForNote(noteId, AUTHOR_ID);
    // then
    assertEquals(1, result.size());
  }

  @Test
  void deleteAttachmentMeta_shouldRemoveMetaAndContent() {
    // given
    NoteAttachmentMeta meta = meta();
    when(metaRepository.findAttachmentMetaById(eq(meta.getId()))).thenReturn(Optional.of(meta));
    // when
    service.deleteAttachmentMeta(meta.getId(), AUTHOR_ID);
    // then
    verify(metaRepository).deleteAttachmentMetaById(meta.getId());
    verify(contentRepository).deleteAttachmentContentByAttachmentId(meta.getId());
  }

  @Test
  void deleteAttachmentMeta_shouldThrowWhenMissing() {
    // given
    UUID id = UUID.randomUUID();
    when(metaRepository.findAttachmentMetaById(eq(id))).thenReturn(Optional.empty());
    // when
    Executable executable = () -> service.deleteAttachmentMeta(id, AUTHOR_ID);
    // then
    assertThrows(NoteAttachmentNotFoundException.class, executable);
    verify(metaRepository, never()).deleteAttachmentMetaById(any());
  }

  @Test
  void uploadAttachmentContent_shouldValidateAndSave() {
    // given
    NoteAttachmentMeta meta = meta();
    when(metaRepository.findAttachmentMetaById(eq(meta.getId()))).thenReturn(Optional.of(meta));
    NoteAttachmentContentValue content = NoteAttachmentContentValue.of(new byte[] {1, 2, 3});
    // when
    service.uploadAttachmentContent(meta.getId(), AUTHOR_ID, content);
    // then
    verify(validator).validateUploadAttachmentContentPayload(content);
    verify(contentRepository).saveAttachmentContent(meta.getId(), content);
  }

  @Test
  void downloadAttachmentContent_shouldEnforceAccessAndReturn() {
    // given
    NoteAttachmentMeta meta = meta();
    NoteAttachmentContentValue content = NoteAttachmentContentValue.of(new byte[] {9});
    when(metaRepository.findAttachmentMetaById(eq(meta.getId()))).thenReturn(Optional.of(meta));
    when(contentRepository.findAttachmentContentByAttachmentId(eq(meta.getId())))
        .thenReturn(Optional.of(content));
    // when
    NoteAttachmentContentValue result = service.downloadAttachmentContent(meta.getId(), AUTHOR_ID);
    // then
    assertEquals(content, result);
  }

  @Test
  void downloadAttachmentContent_shouldEnforceAccessAndThrowWhenMissing() {
    // given
    NoteAttachmentMeta meta = meta();
    when(metaRepository.findAttachmentMetaById(eq(meta.getId()))).thenReturn(Optional.of(meta));
    when(contentRepository.findAttachmentContentByAttachmentId(eq(meta.getId())))
        .thenReturn(Optional.empty());
    // when
    Executable executable = () -> service.downloadAttachmentContent(meta.getId(), AUTHOR_ID);
    // then
    assertThrows(NoteAttachmentNotFoundException.class, executable);
  }

  @Test
  void deleteAttachmentContent_shouldDelete() {
    // given
    NoteAttachmentMeta meta = meta();
    when(metaRepository.findAttachmentMetaById(eq(meta.getId()))).thenReturn(Optional.of(meta));
    // when
    service.deleteAttachmentContent(meta.getId(), AUTHOR_ID);
    // then
    verify(contentRepository).deleteAttachmentContentByAttachmentId(meta.getId());
  }
}
