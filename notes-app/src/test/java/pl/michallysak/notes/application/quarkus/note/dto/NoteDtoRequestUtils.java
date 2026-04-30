package pl.michallysak.notes.application.quarkus.note.dto;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import pl.michallysak.notes.note.model.NotePermission;

public class NoteDtoRequestUtils {

  public static NoteUpdateRequest.NoteUpdateRequestBuilder createNoteUpdateRequestBuilder() {
    return NoteUpdateRequest.builder().title("title").content("content").pinned(false);
  }

  public static NoteResponse.NoteResponseBuilder getNoteResponseBuilder() {
    return NoteResponse.builder()
        .id(UUID.randomUUID())
        .title("title")
        .content("content")
        .created(OffsetDateTime.now())
        .updated(null)
        .pinned(false);
  }

  public static CreateNoteRequest.CreateNoteRequestBuilder getCreateNoteRequestBuilder() {
    return CreateNoteRequest.builder().title("title").content("content");
  }

  public static SetNotePermissionsRequest.SetNotePermissionsRequestBuilder
      createSetNotePermissionsRequestBuilder() {
    return SetNotePermissionsRequest.builder()
        .email("shared@example.com")
        .permissions(Set.of(NotePermission.READ));
  }
}
