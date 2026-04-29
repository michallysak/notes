package pl.michallysak.notes.note.domain;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;
import pl.michallysak.notes.note.exception.NoteAccessException;
import pl.michallysak.notes.note.model.*;
import pl.michallysak.notes.note.validator.NoteValidator;

@Getter
@ToString
public class NoteImpl implements Note {
  private final UUID id;
  private final OffsetDateTime created;
  private String title;
  private String content;
  private OffsetDateTime updated;
  private boolean isPinned;
  private final UUID authorId;
  private NoteStyle style;
  private final NoteValidator noteValidator;
  private final Set<NoteShare> shares = new HashSet<>();

  @Override
  public Set<NoteShare> getShares() {
    return Set.copyOf(shares);
  }

  public NoteImpl(CreateNote createNote, NoteValidator noteValidator) {
    this.noteValidator = noteValidator;
    noteValidator.validateCreateNote(createNote);
    this.authorId = createNote.authorId();
    this.id = UUID.randomUUID();
    this.title = createNote.title();
    this.content = createNote.content();
    this.created = OffsetDateTime.now();
    this.updated = null;
    this.isPinned = false;
  }

  public NoteImpl(NoteValue noteValue, NoteValidator noteValidator) {
    this.noteValidator = noteValidator;
    this.id = noteValue.id();
    this.authorId = noteValue.authorId();
    this.title = noteValue.title();
    this.content = noteValue.content();
    this.created = noteValue.created();
    this.updated = noteValue.updated().orElse(null);
    this.isPinned = noteValue.pinned();
    this.style = noteValue.style();

    if (noteValue.shares() != null) {
      this.shares.addAll(Set.copyOf(noteValue.shares()));
    }
  }

  @Override
  public Optional<OffsetDateTime> getUpdated() {
    return Optional.ofNullable(updated);
  }

  @Override
  public void read(UUID actingUserId) {
    checkPermission(actingUserId, NotePermission.READ);
  }

  @Override
  public void update(NoteUpdate noteUpdate) {
    UUID actingUserId = noteUpdate.actingUserId();
    checkPermission(actingUserId, NotePermission.EDIT);
    noteValidator.validateNoteUpdate(id, noteUpdate, this);
    boolean updatedAny = false;
    if (noteUpdate.title() != null) {
      this.title = noteUpdate.title();
      updatedAny = true;
    }
    if (noteUpdate.content() != null) {
      this.content = noteUpdate.content();
      updatedAny = true;
    }
    if (noteUpdate.pinned() != null) {
      this.isPinned = noteUpdate.pinned();
      updatedAny = true;
    }
    if (noteUpdate.style() != null) {
      this.style = noteUpdate.style();
      updatedAny = true;
    }
    if (updatedAny) {
      this.updated = OffsetDateTime.now();
    }
  }

  @Override
  public void delete(UUID actingUserId) {
    checkOwnership(actingUserId);
  }

  @Override
  public void setPermissions(
      UUID actingUserId, UUID targetUserId, Set<NotePermission> permissions) {
    checkOwnership(actingUserId);

    if (actingUserId.equals(targetUserId)) {
      throw new IllegalArgumentException("Cannot set permissions for yourself");
    }

    if (permissions == null || permissions.isEmpty()) {
      throw new IllegalArgumentException("Permissions cannot be empty");
    }

    shares.removeIf(s -> s.userId().equals(targetUserId));
    shares.add(new NoteShare(targetUserId, Set.copyOf(permissions)));
  }

  @Override
  public void removeAccess(UUID actingUserId, UUID targetUserId) {
    checkOwnership(actingUserId);
    shares.removeIf(s -> s.userId().equals(targetUserId));
  }

  private void checkOwnership(UUID actingUserId) {
    if (!authorId.equals(actingUserId)) {
      throw new NoteAccessException(id, actingUserId);
    }
  }

  private void checkPermission(UUID userId, NotePermission required) {
    if (authorId.equals(userId)) {
      return;
    }

    shares.stream()
        .filter(s -> s.userId().equals(userId))
        .findFirst()
        .filter(s -> s.allows(required))
        .orElseThrow(() -> new NoteAccessException(id, userId));
  }
}
