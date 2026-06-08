package pl.michallysak.notes.note.attachment.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NoteAttachmentContentValueTest {

  @Test
  void of_shouldThrowWhenNull() {
    assertThrows(IllegalArgumentException.class, () -> NoteAttachmentContentValue.of(null));
  }

  @Test
  void value_shouldReturnDefensiveCopy() {
    // given
    byte[] source = {1, 2, 3};
    NoteAttachmentContentValue content = NoteAttachmentContentValue.of(source);
    // when
    byte[] returned = content.value();
    // then
    assertArrayEquals(source, returned);
    assertNotSame(source, returned);
    returned[0] = 9;
    assertArrayEquals(new byte[] {1, 2, 3}, content.value());
  }

  @Test
  void of_shouldCopyInputArray() {
    // given
    byte[] source = {1, 2, 3};
    NoteAttachmentContentValue content = NoteAttachmentContentValue.of(source);
    // when
    source[0] = 9;
    // then
    assertArrayEquals(new byte[] {1, 2, 3}, content.value());
  }

  @Test
  void size_shouldReturnLength() {
    assertEquals(3, NoteAttachmentContentValue.of(new byte[] {1, 2, 3}).size());
  }

  @Test
  void equalsAndHashCode_shouldBeContentBased() {
    NoteAttachmentContentValue a = NoteAttachmentContentValue.of(new byte[] {1, 2});
    NoteAttachmentContentValue b = NoteAttachmentContentValue.of(new byte[] {1, 2});
    NoteAttachmentContentValue c = NoteAttachmentContentValue.of(new byte[] {3, 4});

    assertEquals(a, a);
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertFalse(a.equals(c));
    assertFalse(a.equals(null));
    assertFalse(a.equals("not a content value"));
    assertTrue(a.equals(b));
  }
}
