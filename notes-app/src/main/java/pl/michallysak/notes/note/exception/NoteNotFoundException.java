package pl.michallysak.notes.note.exception;

import pl.michallysak.notes.common.exception.EntityNotFoundException;

public class NoteNotFoundException extends EntityNotFoundException {

  public NoteNotFoundException() {
    super("Note not found");
  }
}
