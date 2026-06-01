package pl.michallysak.notes.note.attachment.model;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class NoteAttachmentContentValue {
  private final byte[] value;

  public static NoteAttachmentContentValue of(byte[] value) {
    if (value == null) {
      throw new IllegalArgumentException("Attachment content cannot be null");
    }
    return new NoteAttachmentContentValue(Arrays.copyOf(value, value.length));
  }

  public byte[] value() {
    return Arrays.copyOf(value, value.length);
  }

  public int size() {
    return value.length;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof NoteAttachmentContentValue that)) {
      return false;
    }
    return Arrays.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(value);
  }
}
