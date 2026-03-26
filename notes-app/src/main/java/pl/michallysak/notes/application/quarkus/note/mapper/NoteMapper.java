package pl.michallysak.notes.application.quarkus.note.mapper;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.mapstruct.Mapper;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;

@Mapper(componentModel = "cdi")
public interface NoteMapper {
  CreateNote mapToCreateNote(CreateNoteRequest createNoteRequest, UUID authorId);

  NoteUpdate mapToNoteUpdate(NoteUpdateRequest noteUpdateRequest, UUID actingUserId);

  NoteResponse mapToNoteResponse(NoteValue noteValue);

  default OffsetDateTime mapToOffsetDateTime(Optional<OffsetDateTime> value) {
    return value.orElse(null);
  }
}
