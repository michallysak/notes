package pl.michallysak.notes.note.model;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import pl.michallysak.notes.note.domain.Note;

@Builder
public record NoteValue(
    UUID id,
    UUID authorId,
    String title,
    String content,
    OffsetDateTime created,
    Optional<OffsetDateTime> updated,
    boolean pinned,
    NoteStyle style,
    Set<NoteShare> shares) {
  public static NoteValue from(Note note) {
    return NoteValue.builder()
        .id(note.getId())
        .authorId(note.getAuthorId())
        .title(note.getTitle())
        .content(note.getContent())
        .created(note.getCreated())
        .pinned(note.isPinned())
        .updated(note.getUpdated())
        .style(note.getStyle())
        .shares(note.getShares())
        .build();
  }
}
