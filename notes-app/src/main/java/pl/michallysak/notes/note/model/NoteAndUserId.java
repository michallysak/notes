package pl.michallysak.notes.note.model;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NoteAndUserId {
  private final UUID noteId;
  private final UUID userId;
}
