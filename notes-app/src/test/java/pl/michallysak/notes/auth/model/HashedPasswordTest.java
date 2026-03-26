package pl.michallysak.notes.auth.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HashedPasswordTest {
  @Test
  void constructor_and_getters_shouldWork() {
    // given
    byte[] hash = {1, 2, 3};
    byte[] salt = {4, 5, 6};
    // when
    HashedPassword hp = new HashedPassword(hash, salt);
    // then
    assertArrayEquals(hash, hp.getHash());
    assertArrayEquals(salt, hp.getSalt());
  }

  @Test
  void equals_and_hashCode_shouldWork() {
    // given
    byte[] hash = {1, 2, 3};
    byte[] salt = {4, 5, 6};
    // when
    HashedPassword hp1 = new HashedPassword(hash, salt);
    HashedPassword hp2 = new HashedPassword(hash, salt);
    // then
    assertEquals(hp1, hp2);
    assertEquals(hp1.hashCode(), hp2.hashCode());
  }
}
