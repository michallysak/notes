package pl.michallysak.notes.note.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.exception.NoteAccessException;
import pl.michallysak.notes.note.model.*;
import pl.michallysak.notes.note.validator.NoteValidator;

@ExtendWith(MockitoExtension.class)
class NoteImplTest {

  @Mock private NoteValidator noteValidator;

  @Test
  void constructor_shouldInitializeFieldsCorrectly() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    // when
    Note note = new NoteImpl(createNote, noteValidator);
    // then
    assertNotNull(note.getId());
    assertEquals(createNote.authorId(), note.getAuthorId());
    assertEquals(createNote.title(), note.getTitle());
    assertEquals(createNote.content(), note.getContent());
    assertNotNull(note.getCreated());
    assertTrue(note.getUpdated().isEmpty());
    assertFalse(note.isPinned());
  }

  @SneakyThrows
  @Test
  void update_shouldModifyFieldsAndSetUpdated_whenNotNullPinned() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    NoteUpdate noteUpdate =
        NoteTestUtils.createNoteUpdateBuilder()
            .title("newTitle")
            .content("newContent")
            .pinned(true)
            .actingUserId(note.getAuthorId())
            .build();
    Thread.sleep(100);
    // when
    note.update(noteUpdate);
    // then
    assertEquals(noteUpdate.title(), note.getTitle());
    assertEquals(noteUpdate.content(), note.getContent());
    assertEquals(noteUpdate.pinned(), note.isPinned());
    assertTrue(note.getUpdated().isPresent());
    assertTrue(note.getUpdated().get().isAfter(note.getCreated()));
  }

  @SneakyThrows
  @Test
  void update_shouldModifyFieldsAndSetUpdated_whenNullPinned() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    NoteUpdate noteUpdate =
        NoteTestUtils.createNoteUpdateBuilder()
            .title("newTitle")
            .content("newContent")
            .actingUserId(note.getAuthorId())
            .build();
    Thread.sleep(100);
    // when
    note.update(noteUpdate);
    // then
    assertEquals(noteUpdate.title(), note.getTitle());
    assertEquals(noteUpdate.content(), note.getContent());
    assertEquals(note.isPinned(), note.isPinned());
    assertTrue(note.getUpdated().isPresent());
    assertTrue(note.getUpdated().get().isAfter(note.getCreated()));
  }

  @SneakyThrows
  @Test
  void update_shouldNotModifyFieldsOrSetUpdated_whenAllFieldsNull() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    Thread.sleep(100);
    NoteUpdate noteUpdate = NoteUpdate.builder().actingUserId(note.getAuthorId()).build();
    // when
    note.update(noteUpdate);
    // then
    assertEquals(createNote.title(), note.getTitle());
    assertEquals(createNote.content(), note.getContent());
    assertFalse(note.isPinned());
    assertTrue(note.getUpdated().isEmpty());
  }

  @SneakyThrows
  @Test
  void update_shouldNotModifyTitle_whenTitleIsNull() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    Thread.sleep(100);
    NoteUpdate noteUpdate =
        NoteUpdate.builder()
            .content("newContent")
            .pinned(true)
            .actingUserId(note.getAuthorId())
            .build();
    // when
    note.update(noteUpdate);
    // then
    assertEquals(createNote.title(), note.getTitle());
    assertEquals(noteUpdate.content(), note.getContent());
    assertTrue(note.isPinned());
    assertTrue(note.getUpdated().isPresent());
    assertTrue(note.getUpdated().get().isAfter(note.getCreated()));
  }

  @SneakyThrows
  @Test
  void update_shouldNotModifyContent_whenContentIsNull() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    Thread.sleep(100);
    NoteUpdate noteUpdate =
        NoteUpdate.builder()
            .title("newTitle")
            .pinned(true)
            .actingUserId(note.getAuthorId())
            .build();
    // when
    note.update(noteUpdate);
    // then
    assertEquals(noteUpdate.title(), note.getTitle());
    assertEquals(createNote.content(), note.getContent());
    assertTrue(note.isPinned());
    assertTrue(note.getUpdated().isPresent());
    assertTrue(note.getUpdated().get().isAfter(note.getCreated()));
  }

  @SneakyThrows
  @Test
  void update_shouldNotModifyPinned_whenPinnedIsNull() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    Thread.sleep(100);
    NoteUpdate noteUpdate =
        NoteUpdate.builder()
            .title("newTitle")
            .content("newContent")
            .actingUserId(note.getAuthorId())
            .build();
    // when
    note.update(noteUpdate);
    // then
    assertEquals(noteUpdate.title(), note.getTitle());
    assertEquals(noteUpdate.content(), note.getContent());
    assertFalse(note.isPinned());
    assertTrue(note.getUpdated().isPresent());
    assertTrue(note.getUpdated().get().isAfter(note.getCreated()));
  }

  @SneakyThrows
  @Test
  void update_shouldModifyStyleAndSetUpdated_whenStyleNotNull() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    NoteStyle noteStyle = NoteStyle.builder().color("#AA11FF").build();
    NoteUpdate noteUpdate =
        NoteUpdate.builder().style(noteStyle).actingUserId(note.getAuthorId()).build();
    Thread.sleep(100);
    // when
    note.update(noteUpdate);
    // then
    assertEquals(noteStyle, note.getStyle());
    assertTrue(note.getUpdated().isPresent());
    assertTrue(note.getUpdated().get().isAfter(note.getCreated()));
  }

  @Test
  void update_shouldThrowNoteAccessException_whenUserIsNotAuthor() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID notAuthorId = UUID.randomUUID();
    NoteUpdate noteUpdate =
        NoteUpdate.builder()
            .title("newTitle")
            .content("newContent")
            .pinned(true)
            .actingUserId(notAuthorId)
            .build();
    // when
    Executable executable = () -> note.update(noteUpdate);
    // then
    assertThrows(NoteAccessException.class, executable);
  }

  @Test
  void delete_shouldThrowNoteAccessException_whenUserIsNotAuthor() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID notAuthorId = UUID.randomUUID();
    // when
    Executable executable = () -> note.delete(notAuthorId);
    // then
    assertThrows(NoteAccessException.class, executable);
  }

  @Test
  void read_shouldThrowNoteAccessException_whenUserIsNotAuthor() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID notAuthorId = UUID.randomUUID();
    // when
    Executable executable = () -> note.read(notAuthorId);
    // then
    assertThrows(NoteAccessException.class, executable);
  }

  @Test
  void constructorWithNoteValue_shouldInitializeAllFieldsAndEmptyShares_whenSharesIsNull() {
    // given
    UUID authorId = UUID.randomUUID();
    NoteValue noteValue =
        NoteTestUtils.createNoteValueBuilder().authorId(authorId).shares(null).build();
    // when
    Note note = new NoteImpl(noteValue, noteValidator);
    // then
    assertEquals(noteValue.id(), note.getId());
    assertEquals(noteValue.authorId(), note.getAuthorId());
    assertEquals(noteValue.title(), note.getTitle());
    assertEquals(noteValue.content(), note.getContent());
    assertEquals(noteValue.created(), note.getCreated());
    assertEquals(noteValue.updated(), note.getUpdated());
    assertEquals(noteValue.pinned(), note.isPinned());
    assertEquals(noteValue.style(), note.getStyle());
    Set<NoteShare> shares = note.getShares(authorId);
    assertNotNull(shares);
    assertTrue(shares.isEmpty());
  }

  @Test
  void constructorWithNoteValue_shouldInitializeAllFieldsAndShares_whenSharesIsNotNull() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID sharedUserId = UUID.randomUUID();
    Set<NoteShare> shares = Set.of(new NoteShare(sharedUserId, Set.of(NotePermission.READ)));
    NoteValue noteValue =
        NoteTestUtils.createNoteValueBuilder().authorId(authorId).shares(shares).build();
    // when
    Note note = new NoteImpl(noteValue, noteValidator);
    // then
    assertEquals(noteValue.id(), note.getId());
    assertEquals(noteValue.authorId(), note.getAuthorId());
    assertEquals(noteValue.title(), note.getTitle());
    assertEquals(noteValue.content(), note.getContent());
    assertEquals(noteValue.created(), note.getCreated());
    assertEquals(noteValue.updated(), note.getUpdated());
    assertEquals(noteValue.pinned(), note.isPinned());
    assertEquals(noteValue.style(), note.getStyle());
    Set<NoteShare> shares1 = note.getShares(authorId);
    assertEquals(1, shares1.size());
    assertTrue(
        shares1.stream()
            .anyMatch(
                s ->
                    s.userId().equals(sharedUserId)
                        && s.permissions().contains(NotePermission.READ)));
  }

  @Test
  void setPermissions_shouldAddNewShare_whenTargetUserNotInShares() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID targetUserId = UUID.randomUUID();
    Set<NotePermission> permissions = Set.of(NotePermission.READ);
    // when
    note.setPermissions(note.getAuthorId(), targetUserId, permissions);
    // then
    assertEquals(1, note.getShares(note.getAuthorId()).size());
    assertTrue(
        note.getShares(note.getAuthorId()).stream()
            .anyMatch(s -> s.userId().equals(targetUserId) && s.permissions().equals(permissions)));
  }

  @Test
  void setPermissions_shouldReplaceExistingShare_whenTargetUserAlreadyInShares() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    Set<NoteShare> initialShares = Set.of(new NoteShare(targetUserId, Set.of(NotePermission.READ)));
    NoteValue noteValue =
        NoteTestUtils.createNoteValueBuilder()
            .authorId(authorId)
            .shares(new HashSet<>(initialShares))
            .build();
    Note note = new NoteImpl(noteValue, noteValidator);
    Set<NotePermission> newPermissions = Set.of(NotePermission.EDIT);
    // when
    note.setPermissions(authorId, targetUserId, newPermissions);
    // then
    assertEquals(1, note.getShares(authorId).size());
    assertTrue(
        note.getShares(authorId).stream()
            .anyMatch(
                s -> s.userId().equals(targetUserId) && s.permissions().equals(newPermissions)));
    assertFalse(
        note.getShares(authorId).stream()
            .anyMatch(
                s ->
                    s.userId().equals(targetUserId)
                        && s.permissions().contains(NotePermission.READ)));
  }

  @Test
  void setPermissions_shouldThrowNoteAccessException_whenNotAuthor() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID notAuthorId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    // when
    Executable executable =
        () -> note.setPermissions(notAuthorId, targetUserId, Set.of(NotePermission.READ));
    // then
    assertThrows(NoteAccessException.class, executable);
  }

  @Test
  void removeAccess_shouldRemoveUserFromShares() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID removedUserId = UUID.randomUUID();
    UUID remainingUserId = UUID.randomUUID();
    Set<NoteShare> initialShares =
        Set.of(
            new NoteShare(removedUserId, Set.of(NotePermission.READ)),
            new NoteShare(remainingUserId, Set.of(NotePermission.EDIT)));
    NoteValue noteValue =
        NoteTestUtils.createNoteValueBuilder()
            .authorId(authorId)
            .shares(new HashSet<>(initialShares))
            .build();
    Note note = new NoteImpl(noteValue, noteValidator);
    // when
    note.removeAccess(authorId, removedUserId);
    // then
    assertEquals(1, note.getShares(authorId).size());
    assertTrue(note.getShares(authorId).stream().anyMatch(s -> s.userId().equals(remainingUserId)));
    assertFalse(note.getShares(authorId).stream().anyMatch(s -> s.userId().equals(removedUserId)));
  }

  @Test
  void removeAccess_shouldThrowNoteAccessException_whenNotAuthor() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID notAuthorId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    // when
    Executable executable = () -> note.removeAccess(notAuthorId, targetUserId);
    // then
    assertThrows(NoteAccessException.class, executable);
  }

  @Test
  void read_shouldSucceed_whenUserIsAuthor() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    //  when
    Executable executable = () -> note.read(note.getAuthorId());
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void read_shouldSucceed_whenUserHasReadPermission() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID readerUserId = UUID.randomUUID();
    Set<NoteShare> shares = Set.of(new NoteShare(readerUserId, Set.of(NotePermission.READ)));
    NoteValue noteValue =
        NoteTestUtils.createNoteValueBuilder()
            .authorId(authorId)
            .shares(new HashSet<>(shares))
            .build();
    Note note = new NoteImpl(noteValue, noteValidator);
    // when
    Executable executable = () -> note.read(readerUserId);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void read_shouldSucceed_whenUserHasEditPermission() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID editorUserId = UUID.randomUUID();
    Set<NoteShare> shares = Set.of(new NoteShare(editorUserId, Set.of(NotePermission.EDIT)));
    NoteValue noteValue =
        NoteTestUtils.createNoteValueBuilder()
            .authorId(authorId)
            .shares(new HashSet<>(shares))
            .build();
    Note note = new NoteImpl(noteValue, noteValidator);
    // when
    Executable executable = () -> note.read(editorUserId);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void read_shouldThrowNoteAccessException_whenUserNoPermissions() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID unauthorizedUserId = UUID.randomUUID();
    NoteValue noteValue =
        NoteTestUtils.createNoteValueBuilder().authorId(authorId).shares(new HashSet<>()).build();
    Note note = new NoteImpl(noteValue, noteValidator);
    // when
    Executable executable = () -> note.read(unauthorizedUserId);
    // then
    assertThrows(NoteAccessException.class, executable);
  }

  @Test
  void read_shouldSucceed_whenNoteIsPublicWithReadPermission() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID randomUserId = UUID.randomUUID();
    NoteValue noteValue =
        NoteTestUtils.createNoteValueBuilder()
            .authorId(authorId)
            .publicShare(
                Optional.of(new NotePublicShare(UUID.randomUUID(), Set.of(NotePermission.READ))))
            .build();
    Note note = new NoteImpl(noteValue, noteValidator);
    // when
    Executable executable = () -> note.read(randomUserId);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void read_shouldSucceed_whenNoteIsPublicWithEditPermission() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID randomUserId = UUID.randomUUID();
    NoteValue noteValue =
        NoteTestUtils.createNoteValueBuilder()
            .authorId(authorId)
            .publicShare(
                Optional.of(new NotePublicShare(UUID.randomUUID(), Set.of(NotePermission.EDIT))))
            .build();
    Note note = new NoteImpl(noteValue, noteValidator);
    // when
    Executable executable = () -> note.read(randomUserId);
    // then
    assertDoesNotThrow(executable);
  }

  @Test
  void delete_shouldSucceed_whenUserIsAuthor() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    // when
    Executable executable = () -> note.delete(note.getAuthorId());
    // then
    assertDoesNotThrow(executable);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void setPermissions_shouldThrowIllegalArgumentException_whenPermissionsIsNullOrEmpty(
      Set<NotePermission> permissions) {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    UUID targetUserId = UUID.randomUUID();
    // when
    Executable executable =
        () -> note.setPermissions(note.getAuthorId(), targetUserId, permissions);
    // then
    assertThrows(IllegalArgumentException.class, executable);
  }

  @Test
  void getShares_shouldReturnAllShares_whenActingUserIsAuthor() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID sharedUserId1 = UUID.randomUUID();
    UUID sharedUserId2 = UUID.randomUUID();
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().authorId(authorId).build();
    Note note = new NoteImpl(createNote, noteValidator);
    note.setPermissions(authorId, sharedUserId1, Set.of(NotePermission.READ));
    note.setPermissions(authorId, sharedUserId2, Set.of(NotePermission.EDIT));
    // when
    Set<NoteShare> visibleShares = note.getShares(authorId);
    // then
    assertEquals(2, visibleShares.size());
    assertTrue(visibleShares.stream().anyMatch(s -> s.userId().equals(sharedUserId1)));
    assertTrue(visibleShares.stream().anyMatch(s -> s.userId().equals(sharedUserId2)));
  }

  @Test
  void getShares_shouldReturnOnlyCurrentUserShare_whenActingUserIsNotAuthor() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID sharedUserId1 = UUID.randomUUID();
    UUID sharedUserId2 = UUID.randomUUID();
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().authorId(authorId).build();
    Note note = new NoteImpl(createNote, noteValidator);
    note.setPermissions(authorId, sharedUserId1, Set.of(NotePermission.READ));
    note.setPermissions(authorId, sharedUserId2, Set.of(NotePermission.EDIT));
    // when
    Set<NoteShare> visibleShares = note.getShares(sharedUserId1);
    // then
    assertEquals(1, visibleShares.size());
    NoteShare share = visibleShares.iterator().next();
    assertEquals(sharedUserId1, share.userId());
    assertEquals(Set.of(NotePermission.READ), share.permissions());
  }

  @Test
  void getShares_shouldReturnEmptySet_whenActingUserIsNotAuthorAndNotShared() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID sharedUserId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().authorId(authorId).build();
    Note note = new NoteImpl(createNote, noteValidator);
    note.setPermissions(authorId, sharedUserId, Set.of(NotePermission.READ));
    // when
    Set<NoteShare> visibleShares = note.getShares(otherUserId);
    // then
    assertTrue(visibleShares.isEmpty());
  }

  @Test
  void getEffectivePermissions_shouldGrantEditToAuthor_evenWhenAuthorHasNoShareEntry() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID editorId = UUID.randomUUID();
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().authorId(authorId).build();
    Note note = new NoteImpl(createNote, noteValidator);
    note.setPermissions(authorId, editorId, Set.of(NotePermission.EDIT));
    // when
    Set<NoteShare> effective = note.getEffectivePermissions(authorId);
    // then
    assertEquals(1, effective.size());
    NoteShare authorShare = effective.iterator().next();
    assertEquals(authorId, authorShare.userId());
    assertTrue(authorShare.allows(NotePermission.READ));
    assertTrue(authorShare.allows(NotePermission.EDIT));
  }

  @Test
  void getEffectivePermissions_shouldReturnOnlyActingUserShare_whenActingUserIsSharedUser() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID sharedUserId = UUID.randomUUID();
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().authorId(authorId).build();
    Note note = new NoteImpl(createNote, noteValidator);
    note.setPermissions(authorId, sharedUserId, Set.of(NotePermission.READ));
    // when
    Set<NoteShare> effective = note.getEffectivePermissions(sharedUserId);
    // then
    assertEquals(1, effective.size());
    NoteShare share = effective.iterator().next();
    assertEquals(sharedUserId, share.userId());
    assertEquals(Set.of(NotePermission.READ), share.permissions());
  }

  @Test
  void getEffectivePermissions_shouldReturnEmptySet_whenActingUserHasNoAccess() {
    // given
    UUID authorId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().authorId(authorId).build();
    Note note = new NoteImpl(createNote, noteValidator);
    // when
    Set<NoteShare> effective = note.getEffectivePermissions(otherUserId);
    // then
    assertTrue(effective.isEmpty());
  }

  @Test
  void makeNotePublic_shouldCreatePublicShareAndReturnId_whenUserIsAuthor() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);

    Set<NotePermission> permissions = Set.of(NotePermission.READ, NotePermission.EDIT);

    // when
    UUID publicShareId = note.makeNotePublic(note.getAuthorId(), permissions);

    // then
    assertNotNull(publicShareId);
    assertTrue(note.getPublicShare().isPresent());

    NotePublicShare publicShare = note.getPublicShare().orElseThrow();
    assertEquals(publicShareId, publicShare.publicShareId());
    assertEquals(permissions, publicShare.permissions());
  }

  @Test
  void makeNotePublic_shouldThrowNoteAccessException_whenUserIsNotAuthor() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);

    UUID notAuthorId = UUID.randomUUID();
    Set<NotePermission> permissions = Set.of(NotePermission.READ);

    // when
    Executable executable = () -> note.makeNotePublic(notAuthorId, permissions);

    // then
    assertThrows(NoteAccessException.class, executable);
  }

  @ParameterizedTest
  @NullAndEmptySource
  void makeNotePublic_shouldThrowIllegalArgumentException_whenPermissionsAreEmpty(
      Set<NotePermission> permissions) {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);

    // when
    Executable executable = () -> note.makeNotePublic(note.getAuthorId(), permissions);

    // then
    assertThrows(IllegalArgumentException.class, executable);
  }

  @Test
  void makeNotePublic_shouldReturnSameId_whenAlreadyPublicWithSamePermissions() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);

    Set<NotePermission> permissions = Set.of(NotePermission.READ);

    UUID firstId = note.makeNotePublic(note.getAuthorId(), permissions);

    // when
    UUID secondId = note.makeNotePublic(note.getAuthorId(), permissions);

    // then
    assertEquals(firstId, secondId);

    NotePublicShare publicShare = note.getPublicShare().orElseThrow();
    assertEquals(firstId, publicShare.publicShareId());
    assertEquals(permissions, publicShare.permissions());
  }

  @Test
  void
      makeNotePublic_shouldReturnSameId_andUpdatePermissions_whenAlreadyPublicWithDifferentPermissions() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);

    Set<NotePermission> firstPermissions = Set.of(NotePermission.READ);
    Set<NotePermission> secondPermissions = Set.of(NotePermission.EDIT);

    UUID firstId = note.makeNotePublic(note.getAuthorId(), firstPermissions);

    // when
    UUID secondId = note.makeNotePublic(note.getAuthorId(), secondPermissions);

    // then
    assertEquals(firstId, secondId);

    NotePublicShare publicShare = note.getPublicShare().orElseThrow();
    assertEquals(firstId, publicShare.publicShareId());
    assertEquals(secondPermissions, publicShare.permissions());
  }

  @Test
  void undoNotePublic_shouldRemovePublicShare_whenUserIsAuthor() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);

    note.makeNotePublic(note.getAuthorId(), Set.of(NotePermission.READ));
    assertTrue(note.getPublicShare().isPresent());

    // when
    note.undoNotePublic(note.getAuthorId());

    // then
    assertTrue(note.getPublicShare().isEmpty());
  }

  @Test
  void undoNotePublic_shouldThrowNoteAccessException_whenUserIsNotAuthor() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);

    note.makeNotePublic(note.getAuthorId(), Set.of(NotePermission.READ));

    UUID notAuthorId = UUID.randomUUID();

    // when
    Executable executable = () -> note.undoNotePublic(notAuthorId);

    // then
    assertThrows(NoteAccessException.class, executable);
  }
}
