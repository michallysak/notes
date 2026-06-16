package pl.michallysak.notes.note.attachment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.note.attachment.domain.NoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.domain.NoteAttachmentMetaImpl;
import pl.michallysak.notes.note.attachment.exception.NoteAttachmentNotFoundException;
import pl.michallysak.notes.note.attachment.model.CreateNoteAttachmentMeta;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentContentValue;
import pl.michallysak.notes.note.attachment.model.NoteAttachmentMetaValue;
import pl.michallysak.notes.note.attachment.repository.NoteAttachmentContentRepository;
import pl.michallysak.notes.note.attachment.repository.NoteAttachmentMetaRepository;
import pl.michallysak.notes.note.attachment.validator.NoteAttachmentValidator;
import pl.michallysak.notes.note.exception.NoteAccessException;
import pl.michallysak.notes.note.model.NotePermission;
import pl.michallysak.notes.note.model.NoteShare;
import pl.michallysak.notes.note.service.NoteService;

@ExtendWith(MockitoExtension.class)
class NoteAttachmentServiceImplTest {

  @Mock private NoteAttachmentMetaRepository metaRepository;
  @Mock private NoteAttachmentContentRepository contentRepository;
  @Mock private NoteAttachmentValidator validator;
  @Mock private NoteService noteService;

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
    Set<NoteShare> shares = new HashSet<>();
    shares.add(new NoteShare(AUTHOR_ID, Set.of(NotePermission.READ, NotePermission.EDIT)));
    when(noteService.getEffectivePermissions(eq(meta.getNoteId()), eq(AUTHOR_ID)))
        .thenReturn(shares);
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
    // Mock noteService to throw exception (user doesn't have access to note)
    when(noteService.getEffectivePermissions(eq(meta.getNoteId()), eq(intruder)))
        .thenThrow(new NoteAccessException(meta.getNoteId(), intruder));
    // when
    Executable executable = () -> service.getAttachmentMeta(meta.getId(), intruder);
    // then
    assertThrows(NoteAccessException.class, executable);
  }

  @Test
  void getAttachmentMetasForNote_shouldEnforceAccessPerItem() {
    // given
    NoteAttachmentMeta meta = meta();
    UUID noteId = meta.getNoteId();
    when(metaRepository.findAttachmentMetaByNoteId(eq(noteId))).thenReturn(List.of(meta));
    Set<NoteShare> shares = new HashSet<>();
    shares.add(new NoteShare(AUTHOR_ID, Set.of(NotePermission.READ, NotePermission.EDIT)));
    when(noteService.getEffectivePermissions(eq(noteId), eq(AUTHOR_ID))).thenReturn(shares);
    // when
    List<NoteAttachmentMetaValue> result = service.getAttachmentMetasForNote(noteId, AUTHOR_ID);
    // then
    assertEquals(1, result.size());
  }

  @Test
  void getAttachmentMetasForNote_shouldAllowNoteOwner_whenAttachmentUploadedByEditor() {
    // given
    // an editor (not the note owner) uploaded the attachment
    UUID noteOwnerId = UUID.randomUUID();
    UUID editorId = UUID.randomUUID();
    CreateNoteAttachmentMeta create =
        CreateNoteAttachmentMeta.builder()
            .noteId(UUID.randomUUID())
            .authorId(editorId)
            .fileName("file.txt")
            .contentType("text/plain")
            .size(5)
            .build();
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(create, validator);
    UUID noteId = meta.getNoteId();
    when(metaRepository.findAttachmentMetaByNoteId(eq(noteId))).thenReturn(List.of(meta));
    // the note owner's effective permission on the note is EDIT (implicitly, as the author)
    Set<NoteShare> ownerPermissions =
        Set.of(new NoteShare(noteOwnerId, Set.of(NotePermission.EDIT)));
    when(noteService.getEffectivePermissions(eq(noteId), eq(noteOwnerId)))
        .thenReturn(ownerPermissions);
    // when
    List<NoteAttachmentMetaValue> result = service.getAttachmentMetasForNote(noteId, noteOwnerId);
    // then
    assertEquals(1, result.size());
    assertEquals(editorId, result.getFirst().authorId());
  }

  @Test
  void deleteAttachmentMeta_shouldRemoveMetaAndContent() {
    // given
    NoteAttachmentMeta meta = meta();
    when(metaRepository.findAttachmentMetaById(eq(meta.getId()))).thenReturn(Optional.of(meta));
    Set<NoteShare> shares = new HashSet<>();
    shares.add(new NoteShare(AUTHOR_ID, Set.of(NotePermission.EDIT)));
    when(noteService.getEffectivePermissions(eq(meta.getNoteId()), eq(AUTHOR_ID)))
        .thenReturn(shares);
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
    Set<NoteShare> shares = new HashSet<>();
    shares.add(new NoteShare(AUTHOR_ID, Set.of(NotePermission.EDIT)));
    when(noteService.getEffectivePermissions(eq(meta.getNoteId()), eq(AUTHOR_ID)))
        .thenReturn(shares);
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
    Set<NoteShare> shares = new HashSet<>();
    shares.add(new NoteShare(AUTHOR_ID, Set.of(NotePermission.READ, NotePermission.EDIT)));
    when(noteService.getEffectivePermissions(eq(meta.getNoteId()), eq(AUTHOR_ID)))
        .thenReturn(shares);
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
    Set<NoteShare> shares = new HashSet<>();
    shares.add(new NoteShare(AUTHOR_ID, Set.of(NotePermission.READ, NotePermission.EDIT)));
    when(noteService.getEffectivePermissions(eq(meta.getNoteId()), eq(AUTHOR_ID)))
        .thenReturn(shares);
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
    Set<NoteShare> shares = new HashSet<>();
    shares.add(new NoteShare(AUTHOR_ID, Set.of(NotePermission.EDIT)));
    when(noteService.getEffectivePermissions(eq(meta.getNoteId()), eq(AUTHOR_ID)))
        .thenReturn(shares);
    // when
    service.deleteAttachmentContent(meta.getId(), AUTHOR_ID);
    // then
    verify(contentRepository).deleteAttachmentContentByAttachmentId(meta.getId());
  }
}
