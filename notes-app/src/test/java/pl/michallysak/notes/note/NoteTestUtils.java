package pl.michallysak.notes.note;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import pl.michallysak.notes.note.model.*;
import pl.michallysak.notes.user.service.NoAuthCurrentUserProvider;

public class NoteTestUtils {

  private static final UUID AUTHOR_ID = new NoAuthCurrentUserProvider().getCurrentUserId();

  public static CreateNote.CreateNoteBuilder createCreateNoteBuilder() {
    return CreateNote.builder().title("validTitle").content("validContent").authorId(AUTHOR_ID);
  }

  public static NoteUpdate.NoteUpdateBuilder createNoteUpdateBuilder() {
    return NoteUpdate.builder().title("validTitle").content("validContent").pinned(null);
  }

  public static NoteValue.NoteValueBuilder createNoteValueBuilder() {
    return NoteValue.builder()
        .id(UUID.randomUUID())
        .title("validTitle")
        .content("validContent")
        .created(OffsetDateTime.now())
        .updated(Optional.empty())
        .pinned(true);
  }

  public static NotePagedQuery createNotePagedQuery(
      Boolean isShared, Boolean isPinned, int page, int size, List<FieldSort> fieldSorts) {
    return createNotePagedQuery(isShared, isPinned, page, size, fieldSorts, null);
  }

  public static NotePagedQuery createNotePagedQuery(
      Boolean isShared,
      Boolean isPinned,
      int page,
      int size,
      List<FieldSort> fieldSorts,
      String searchQuery) {
    return new NotePagedQuery() {
      @Override
      public int getPage() {
        return page;
      }

      @Override
      public int getSize() {
        return size;
      }

      @Override
      public Boolean getIsShared() {
        return isShared;
      }

      @Override
      public Boolean getIsPinned() {
        return isPinned;
      }

      @Override
      public List<FieldSort> getSort() {
        return fieldSorts;
      }

      @Override
      public String getSearchQuery() {
        return searchQuery;
      }
    };
  }
}
