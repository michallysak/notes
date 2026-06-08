package pl.michallysak.notes.note.domain;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import pl.michallysak.notes.note.model.NoteStyle;

public interface Note extends NoteActions {
  UUID getId();

  String getTitle();

  String getContent();

  OffsetDateTime getCreated();

  Optional<OffsetDateTime> getUpdated();

  boolean isPinned();

  UUID getAuthorId();

  NoteStyle getStyle();
}
