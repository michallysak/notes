package pl.michallysak.notes.note.domain.event;

import java.util.UUID;

public class NoteCreatedEvent implements DomainEvent {
  public final UUID id;
  public final String content;

  public NoteCreatedEvent(UUID id, String content) {
    this.id = id;
    this.content = content;
  }
}
