package pl.michallysak.notes.note.exception;

import java.util.UUID;

public class NoteAccessException extends RuntimeException {
  public NoteAccessException(UUID noteId, UUID actingUserId) {
    super("User %s is not the author of note %s".formatted(actingUserId, noteId));
  }
}
