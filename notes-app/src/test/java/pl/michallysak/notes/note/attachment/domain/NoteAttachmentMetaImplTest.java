package pl.michallysak.notes.note.attachment.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.Set;
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
import pl.michallysak.notes.note.model.NotePermission;
import pl.michallysak.notes.note.model.NoteShare;

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
  void actions_shouldAllowAuthorWithAnyShare() {
    // given
    UUID authorId = UUID.randomUUID();
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(authorId), validator);
    Set<NoteShare> emptyShares = Set.of();
    // when
    // attachment author can perform all operations regardless of note shares
    Executable executable =
        () -> {
          meta.read(authorId, emptyShares);
          meta.delete(authorId, emptyShares);
          meta.uploadContent(authorId, emptyShares);
          meta.downloadContent(authorId, emptyShares);
          meta.deleteContent(authorId, emptyShares);
        };
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void uploadContent_shouldRequireEditPermission() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    Set<NoteShare> readOnlyShares = Set.of(new NoteShare(user, Set.of(NotePermission.READ)));
    // when
    Executable executable = () -> meta.uploadContent(user, readOnlyShares);
    // then
    assertThrows(NoteAttachmentAccessException.class, executable);
  }

  @Test
  void delete_shouldRequireEditPermission() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    Set<NoteShare> readOnlyShares = Set.of(new NoteShare(user, Set.of(NotePermission.READ)));
    // when
    Executable executable = () -> meta.delete(user, readOnlyShares);
    // then
    assertThrows(NoteAttachmentAccessException.class, executable);
  }

  @Test
  void deleteContent_shouldRequireEditPermission() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    Set<NoteShare> readOnlyShares = Set.of(new NoteShare(user, Set.of(NotePermission.READ)));
    // when
    Executable executable = () -> meta.deleteContent(user, readOnlyShares);
    // then
    assertThrows(NoteAttachmentAccessException.class, executable);
  }

  @Test
  void read_shouldAllowUserWithReadPermission() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    Set<NoteShare> readShares = Set.of(new NoteShare(user, Set.of(NotePermission.READ)));
    // when
    Executable executable = () -> meta.read(user, readShares);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void read_shouldRejectUserNotInPermissions() {
    // given
    // acting user has no effective permission entry for the note
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    UUID otherUser = UUID.randomUUID();
    Set<NoteShare> permissionsForOtherUser =
        Set.of(new NoteShare(otherUser, Set.of(NotePermission.EDIT)));
    // when
    Executable executable = () -> meta.read(user, permissionsForOtherUser);
    // then
    // read must deny when the acting user has no permission
    assertThrows(NoteAttachmentAccessException.class, executable);
  }

  @Test
  void read_shouldRejectUserWithEmptyPermissions() {
    // given
    // acting user has no effective permissions at all
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    Set<NoteShare> emptyPermissions = Set.of();
    // when
    Executable executable = () -> meta.read(user, emptyPermissions);
    // then
    // read must deny with empty permissions
    assertThrows(NoteAttachmentAccessException.class, executable);
  }

  @Test
  void read_shouldAllowUserWithEditPermission() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    Set<NoteShare> editShares = Set.of(new NoteShare(user, Set.of(NotePermission.EDIT)));
    // when
    Executable executable = () -> meta.read(user, editShares);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void uploadContent_shouldAllowUserWithEditPermission() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    Set<NoteShare> editShares = Set.of(new NoteShare(user, Set.of(NotePermission.EDIT)));
    // when
    Executable executable = () -> meta.uploadContent(user, editShares);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void uploadContent_shouldRejectUserWithEmptyShares() {
    // given - EDIT operations should not allow empty shares even if READ would
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    Set<NoteShare> emptyShares = Set.of();
    // when
    // then
    Executable executable = () -> meta.uploadContent(user, emptyShares);
    // EDIT should deny with empty shares
    assertThrows(NoteAttachmentAccessException.class, executable);
  }

  @Test
  void delete_shouldAllowUserWithEditPermission() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    Set<NoteShare> editShares = Set.of(new NoteShare(user, Set.of(NotePermission.EDIT)));
    // when
    Executable executable = () -> meta.delete(user, editShares);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void deleteContent_shouldAllowUserWithEditPermission() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    Set<NoteShare> editShares = Set.of(new NoteShare(user, Set.of(NotePermission.EDIT)));
    // when
    Executable executable = () -> meta.deleteContent(user, editShares);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void downloadContent_shouldAllowUserWithReadPermission() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    Set<NoteShare> readShares = Set.of(new NoteShare(user, Set.of(NotePermission.READ)));
    // when
    Executable executable = () -> meta.downloadContent(user, readShares);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void downloadContent_shouldAllowUserWithEditPermission() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    Set<NoteShare> editShares = Set.of(new NoteShare(user, Set.of(NotePermission.EDIT)));
    // when
    Executable executable = () -> meta.downloadContent(user, editShares);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void uploadContent_shouldRejectUserNotInShares() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    UUID otherUser = UUID.randomUUID();
    Set<NoteShare> sharesForOtherUser =
        Set.of(new NoteShare(otherUser, Set.of(NotePermission.EDIT)));
    // when
    Executable executable = () -> meta.uploadContent(user, sharesForOtherUser);
    // then
    assertThrows(NoteAttachmentAccessException.class, executable);
  }

  @Test
  void uploadContent_shouldRejectUserWithReadOnly() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    Set<NoteShare> readOnlyShares = Set.of(new NoteShare(user, Set.of(NotePermission.READ)));
    // when
    Executable executable = () -> meta.uploadContent(user, readOnlyShares);
    // then
    assertThrows(NoteAttachmentAccessException.class, executable);
  }

  @Test
  void delete_shouldRejectUserNotInShares() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    UUID otherUser = UUID.randomUUID();
    Set<NoteShare> sharesForOtherUser =
        Set.of(new NoteShare(otherUser, Set.of(NotePermission.EDIT)));
    // when
    Executable executable = () -> meta.delete(user, sharesForOtherUser);
    // then
    assertThrows(NoteAttachmentAccessException.class, executable);
  }

  @Test
  void deleteContent_shouldRejectUserNotInShares() {
    // given
    NoteAttachmentMeta meta = new NoteAttachmentMetaImpl(createMeta(UUID.randomUUID()), validator);
    UUID user = UUID.randomUUID();
    UUID otherUser = UUID.randomUUID();
    Set<NoteShare> sharesForOtherUser =
        Set.of(new NoteShare(otherUser, Set.of(NotePermission.EDIT)));
    // when
    Executable executable = () -> meta.deleteContent(user, sharesForOtherUser);
    // then
    assertThrows(NoteAttachmentAccessException.class, executable);
  }
}
