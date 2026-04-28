package pl.michallysak.notes.note.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NoteShareTest {

  @Test
  void allows_shouldReturnTrue_whenPermissionIsDirectlyInSet() {
    // given
    UUID userId = UUID.randomUUID();
    NoteShare noteShare = new NoteShare(userId, Set.of(NotePermission.READ));
    // when
    boolean result = noteShare.allows(NotePermission.READ);
    // then
    assertTrue(result);
  }

  @Test
  void allows_shouldReturnTrue_whenEditPermissionIsInSet() {
    // given
    UUID userId = UUID.randomUUID();
    NoteShare noteShare = new NoteShare(userId, Set.of(NotePermission.EDIT));
    // when
    boolean resultRead = noteShare.allows(NotePermission.READ);
    boolean resultEdit = noteShare.allows(NotePermission.EDIT);
    // then
    assertTrue(resultRead);
    assertTrue(resultEdit);
  }

  @Test
  void allows_shouldReturnTrue_whenEditPermissionIsInSetAndCheckingRead() {
    // given
    UUID userId = UUID.randomUUID();
    NoteShare noteShare = new NoteShare(userId, Set.of(NotePermission.EDIT));
    // when
    boolean result = noteShare.allows(NotePermission.READ);
    // then
    assertTrue(result);
  }

  @Test
  void allows_shouldReturnFalse_whenPermissionIsNotInSetAndEditIsNotInSet() {
    // given
    UUID userId = UUID.randomUUID();
    NoteShare noteShare = new NoteShare(userId, Set.of());
    // when
    boolean result = noteShare.allows(NotePermission.READ);
    // then
    assertFalse(result);
  }

  @Test
  void allows_shouldReturnFalse_whenCheckingEditButOnlyReadIsInSet() {
    // given
    UUID userId = UUID.randomUUID();
    NoteShare noteShare = new NoteShare(userId, Set.of(NotePermission.READ));
    // when
    boolean result = noteShare.allows(NotePermission.EDIT);
    // then
    assertFalse(result);
  }

  @Test
  void allows_shouldReturnTrue_whenBothPermissionsAreInSet() {
    // given
    UUID userId = UUID.randomUUID();
    NoteShare noteShare = new NoteShare(userId, Set.of(NotePermission.READ, NotePermission.EDIT));
    // when
    boolean resultRead = noteShare.allows(NotePermission.READ);
    boolean resultEdit = noteShare.allows(NotePermission.EDIT);
    // then
    assertTrue(resultRead);
    assertTrue(resultEdit);
  }

  @Test
  void constructor_shouldCreateRecordWithUserIdAndPermissions() {
    // given
    UUID userId = UUID.randomUUID();
    Set<NotePermission> permissions = Set.of(NotePermission.READ, NotePermission.EDIT);
    // when
    NoteShare noteShare = new NoteShare(userId, permissions);
    // then
    assertEquals(userId, noteShare.userId());
    assertEquals(permissions, noteShare.permissions());
  }
}
