package pl.michallysak.notes.note.domain.event;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.domain.Note;

@Getter
@RequiredArgsConstructor
public class NoteCreatedEvent implements DomainEvent<NoteCreatedEvent.Payload> {
  private final UUID id;
  private final Payload payload;
  private final List<UUID> recipients;

  @Getter
  @RequiredArgsConstructor
  public static class Payload {
    private final String title;
    private final String content;
  }

  public static NoteCreatedEvent from(Note note) {
    Payload payload = new Payload(note.getTitle(), note.getContent());
    UUID authorId = note.getAuthorId();
    return new NoteCreatedEvent(note.getId(), payload, Collections.singletonList(authorId));
  }
}
