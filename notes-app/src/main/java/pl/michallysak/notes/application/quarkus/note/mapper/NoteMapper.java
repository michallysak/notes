package pl.michallysak.notes.application.quarkus.note.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import pl.michallysak.notes.application.quarkus.note.dto.*;
import pl.michallysak.notes.application.quarkus.note.persistence.NoteEntity;
import pl.michallysak.notes.application.quarkus.note.persistence.NotePublicShareEntity;
import pl.michallysak.notes.application.quarkus.note.persistence.NoteShareEntity;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;
import pl.michallysak.notes.note.model.*;
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

  @Mapping(target = "shares", ignore = true)
  @Mapping(target = "publicShare", ignore = true)
  public abstract NoteResponse mapToNoteResponse(NoteValue noteValue);

  @Mapping(target = "shares", source = "shares")
  @Mapping(target = "publicShare", ignore = true)
  public abstract NoteResponse mapToNoteResponse(
      NoteValue noteValue, List<NoteShareResponse> shares);

  public NoteResponse mapToNoteResponseWithPublicShare(
      NoteValue noteValue, List<NoteShareResponse> shares) {
    NoteResponse response = mapToNoteResponse(noteValue, shares);
    response.setPublicShare(mapToNotePublicShareResponse(noteValue.publicShare()));
    return response;
  }

  @Mapping(target = "email", ignore = true)
  public abstract NoteShareResponse mapToNoteShareResponse(NoteShare noteShare);

  @Mapping(target = "email", source = "email.value")
  @Mapping(target = "userId", source = "noteShare.userId")
  @Mapping(target = "permissions", source = "noteShare.permissions")
  public abstract NoteShareResponse mapToNoteShareResponse(NoteShare noteShare, Email email);

  @Mapping(target = "author", expression = "java(new UserEntity())")
  @Mapping(target = "author.id", source = "authorId")
  @Mapping(
      target = "shares",
      expression = "java(noteSharesToDomainNoteShareEntities(note.getShares(note.getAuthorId())))")
  public abstract NoteEntity mapToEntity(Note note);

  @Mapping(target = "authorId", source = "author.id")
  @Mapping(target = "shares", source = "shares")
  public abstract NoteValue mapToNoteValue(NoteEntity noteEntity);

  public Note mapToDomain(NoteEntity noteEntity) {
    return new NoteImpl(mapToNoteValue(noteEntity), noteValidator);
  }

  @Mapping(target = "id", source = "publicShareId")
  @Mapping(target = "note", ignore = true)
  protected abstract NotePublicShareEntity notePublicShareToEntity(NotePublicShare value);

  @Mapping(target = "publicShareId", source = "id")
  protected abstract NotePublicShare entityToNotePublicShare(NotePublicShareEntity value);

  protected NotePublicShareEntity optionalNotePublicShareToEntity(Optional<NotePublicShare> value) {

    return value != null && value.isPresent() ? notePublicShareToEntity(value.get()) : null;
  }

  protected Optional<NotePublicShare> entityToOptionalNotePublicShare(NotePublicShareEntity value) {

    return Optional.ofNullable(value).map(this::entityToNotePublicShare);
  }

  protected NotePublicShareResponse mapToNotePublicShareResponse(NotePublicShare notePublicShare) {
    if (notePublicShare == null) {
      return null;
    }
    return NotePublicShareResponse.builder()
        .publicShareId(notePublicShare.publicShareId())
        .permissions(notePublicShare.permissions())
        .build();
  }

  protected NotePublicShareResponse mapToNotePublicShareResponse(
      Optional<NotePublicShare> publicShare) {
    return publicShare != null && publicShare.isPresent()
        ? mapToNotePublicShareResponse(publicShare.get())
        : null;
  }

  protected OffsetDateTime mapToOffsetDateTime(Optional<OffsetDateTime> value) {
    return value == null ? null : value.orElse(null);
  }

  protected Optional<OffsetDateTime> mapToOptionalOffsetDateTime(OffsetDateTime value) {
    return Optional.ofNullable(value);
  }

  protected NoteShare noteShareEntityToDomainNoteShare(NoteShareEntity entity) {
    if (entity == null) {
      return null;
    }
    if (entity.getUser() == null || entity.getUser().getId() == null) {
      return null;
    }
    return new NoteShare(entity.getUser().getId(), entity.getPermissions());
  }

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "note", ignore = true)
  @Mapping(target = "user", source = "userId")
  protected abstract NoteShareEntity noteShareToDomainNoteShareEntity(NoteShare noteShare);

  protected Set<NoteShareEntity> noteSharesToDomainNoteShareEntities(Set<NoteShare> shares) {
    if (shares == null) {
      return null;
    }
    return shares.stream().map(this::noteShareToDomainNoteShareEntity).collect(Collectors.toSet());
  }

  protected UserEntity userIdToUserEntity(UUID userId) {
    if (userId == null) {
      return null;
    }
    UserEntity user = new UserEntity();
    user.setId(userId);
    return user;
  }
}
