package pl.michallysak.notes.application.quarkus.note.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;
import pl.michallysak.notes.application.quarkus.note.persistence.NoteEntity;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.validator.NoteValidator;
import pl.michallysak.notes.user.repository.UserEntity;

@Setter(onMethod_ = @Inject)
@Mapper(
    componentModel = MappingConstants.ComponentModel.CDI,
    imports = {UserEntity.class})
@ApplicationScoped
public abstract class NoteMapper {
  protected NoteValidator noteValidator;

  public abstract CreateNote mapToCreateNote(CreateNoteRequest createNoteRequest, UUID authorId);

  public abstract NoteUpdate mapToNoteUpdate(
      NoteUpdateRequest noteUpdateRequest, UUID actingUserId);

  public abstract NoteResponse mapToNoteResponse(NoteValue noteValue);

  @Mapping(target = "author", expression = "java(new UserEntity())")
  @Mapping(target = "author.id", source = "authorId")
  public abstract NoteEntity mapToEntity(Note note);

  @Mapping(target = "authorId", source = "author.id")
  public abstract NoteValue mapToNoteValue(NoteEntity noteEntity);

  public Note mapToDomain(NoteEntity noteEntity) {
    return new NoteImpl(mapToNoteValue(noteEntity), noteValidator);
  }

  protected OffsetDateTime mapToOffsetDateTime(Optional<OffsetDateTime> value) {
    return value.orElse(null);
  }

  protected Optional<OffsetDateTime> mapToOptionalOffsetDateTime(OffsetDateTime value) {
    return Optional.ofNullable(value);
  }
}
