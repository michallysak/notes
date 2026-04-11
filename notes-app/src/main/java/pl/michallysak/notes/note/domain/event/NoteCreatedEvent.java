package pl.michallysak.notes.note.domain.event;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.domain.Note;

@Getter
@RequiredArgsConstructor
public class NoteCreatedEvent implements DomainEvent<NoteCreatedEvent.Payload> {
  private final UUID id;
  private final Payload payload;
  private final Set<UUID> recipients;

  @Getter
  @NoArgsConstructor(force = true)
  @RequiredArgsConstructor
  public static class Payload {
    private final String title;
    private final String content;
  }

  public static NoteCreatedEvent from(Note note) {
    Payload payload = new Payload(note.getTitle(), note.getContent());
    UUID authorId = note.getAuthorId();
    return new NoteCreatedEvent(note.getId(), payload, Collections.singleton(authorId));
  }
}
