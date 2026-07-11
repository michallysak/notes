package pl.michallysak.notes.application.quarkus.note.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils;
import pl.michallysak.notes.application.quarkus.note.dto.NotePublicShareResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteShareResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteStyleDTO;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;
import pl.michallysak.notes.application.quarkus.note.persistence.NoteEntity;
import pl.michallysak.notes.application.quarkus.note.persistence.NotePublicShareEntity;
import pl.michallysak.notes.application.quarkus.note.persistence.NoteShareEntity;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;
import pl.michallysak.notes.note.model.*;
import pl.michallysak.notes.note.validator.NoteValidator;
import pl.michallysak.notes.user.repository.UserEntity;

@ExtendWith(MockitoExtension.class)
class NoteMapperTest {

  private static final UUID AUTHOR_ID = UUID.randomUUID();

  @Mock private NoteValidator noteValidator;
  private NoteMapperImpl noteMapper;

  @BeforeEach
  void setUp() {
    this.noteMapper = new NoteMapperImpl();
    noteMapper.setNoteValidator(noteValidator);
  }

  @Test
  void mapToNoteUpdate_shouldMapCorrectly() {
    // given
    NoteUpdateRequest request = NoteDtoRequestUtils.createNoteUpdateRequestBuilder().build();
    UUID actingUserId = AUTHOR_ID;
    // when
    NoteUpdate noteUpdate = noteMapper.mapToNoteUpdate(request, actingUserId);
    // then
    assertEquals(request.getTitle(), noteUpdate.title());
    assertEquals(request.getContent(), noteUpdate.content());
    assertEquals(request.getPinned(), noteUpdate.pinned());
    assertEquals(actingUserId, noteUpdate.actingUserId());
  }

  @Test
  void mapToNoteUpdate_shouldReturnNull_whenRequestAndActingUserIdNull() {
    // given
    NoteUpdateRequest request = null;
    UUID actingUserId = null;
    // when
    NoteUpdate noteUpdate = noteMapper.mapToNoteUpdate(request, actingUserId);
    // then
    assertNull(noteUpdate);
  }

  @Test
  void mapToNoteUpdate_shouldReturnOnlySetActingUserId_whenRequestNullAndValidActingUserId() {
    // given
    NoteUpdateRequest request = null;
    UUID actingUserId = AUTHOR_ID;
    // when
    NoteUpdate noteUpdate = noteMapper.mapToNoteUpdate(request, actingUserId);
    // then
    assertNotNull(noteUpdate);
    assertEquals(actingUserId, noteUpdate.actingUserId());
    assertNull(noteUpdate.title());
    assertNull(noteUpdate.content());
    assertNull(noteUpdate.pinned());
  }

  @Test
  void
      mapToNoteUpdate_shouldReturnOnlySetTitleAndContentAndPinned_whenRequestValidAndActingUserIdNull() {
    // given
    NoteUpdateRequest request = NoteDtoRequestUtils.createNoteUpdateRequestBuilder().build();
    UUID actingUserId = null;
    // when
    NoteUpdate noteUpdate = noteMapper.mapToNoteUpdate(request, actingUserId);
    // then
    assertNotNull(noteUpdate);
    assertNull(noteUpdate.actingUserId());
    assertEquals(request.getTitle(), noteUpdate.title());
    assertEquals(request.getContent(), noteUpdate.content());
    assertEquals(request.getPinned(), noteUpdate.pinned());
  }

  @Test
  void toNoteResponse_shouldMapCorrectly_whenNull() {
    // given
    NoteValue value = null;
    // when
    NoteResponse noteResponse = noteMapper.mapToNoteResponse(value);
    // then
    assertNull(noteResponse);
  }

  @ParameterizedTest
  @MethodSource("provideNoteValues")
  void toNoteResponse_shouldMapCorrectly(
      @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<OffsetDateTime> updated) {
    // given
    NoteShare share = new NoteShare(UUID.randomUUID(), Set.of(NotePermission.READ));
    NoteValue value =
        NoteTestUtils.createNoteValueBuilder().updated(updated).shares(Set.of(share)).build();
    // when
    NoteResponse noteResponse = noteMapper.mapToNoteResponse(value);
    // then
    assertEquals(value.id(), noteResponse.getId());
    assertEquals(value.authorId(), noteResponse.getAuthorId());
    assertEquals(value.title(), noteResponse.getTitle());
    assertEquals(value.content(), noteResponse.getContent());
    assertEquals(value.created(), noteResponse.getCreated());
    assertEquals(value.pinned(), noteResponse.isPinned());
    assertEquals(updated.orElse(null), noteResponse.getUpdated());
    assertNull(noteResponse.getShares());
  }

  @ParameterizedTest
  @MethodSource("provideNoteValues")
  void toNoteResponse_shouldMapCorrectly_withShares(
      @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<OffsetDateTime> updated) {
    // given
    UUID userId = UUID.randomUUID();
    NoteShare share = new NoteShare(userId, Set.of(NotePermission.READ, NotePermission.EDIT));
    NoteValue value =
        NoteTestUtils.createNoteValueBuilder().updated(updated).shares(Set.of(share)).build();
    Email email = Email.of("user@example.com");
    NoteShareResponse shareResponse = noteMapper.mapToNoteShareResponse(share, email);
    // when
    NoteResponse noteResponse = noteMapper.mapToNoteResponse(value, List.of(shareResponse));
    // then
    assertEquals(value.id(), noteResponse.getId());
    assertEquals(value.authorId(), noteResponse.getAuthorId());
    assertEquals(value.title(), noteResponse.getTitle());
    assertEquals(value.content(), noteResponse.getContent());
    assertEquals(value.created(), noteResponse.getCreated());
    assertEquals(value.pinned(), noteResponse.isPinned());
    assertEquals(updated.orElse(null), noteResponse.getUpdated());
    assertNotNull(noteResponse.getShares());
    assertEquals(1, noteResponse.getShares().size());
    assertEquals(userId, noteResponse.getShares().getFirst().getUserId());
    assertEquals("user@example.com", noteResponse.getShares().getFirst().getEmail());
    assertEquals(
        Set.of(NotePermission.READ, NotePermission.EDIT),
        noteResponse.getShares().getFirst().getPermissions());
  }

  public static Stream<Arguments> provideNoteValues() {
    return Stream.of(
        Arguments.of(Optional.empty()), Arguments.of(Optional.of(OffsetDateTime.now())));
  }

  @Test
  void mapToCreateNote_shouldMapCorrectly() {
    // given
    CreateNoteRequest request = NoteDtoRequestUtils.getCreateNoteRequestBuilder().build();
    // when
    CreateNote createNote = noteMapper.mapToCreateNote(request, AUTHOR_ID);
    // then
    assertEquals(request.getTitle(), createNote.title());
    assertEquals(request.getContent(), createNote.content());
    assertEquals(AUTHOR_ID, createNote.authorId());
  }

  @Test
  void mapToCreateNote_shouldReturnNull_whenRequestAndAutorIdNull() {
    // given
    CreateNoteRequest request = null;
    UUID authorId = null;
    // when
    CreateNote createNote = noteMapper.mapToCreateNote(request, authorId);
    // then
    assertNull(createNote);
  }

  @Test
  void mapToCreateNote_shouldReturnOnlySetAuthorId_whenRequestNullAndValidAuthorIdId() {
    // given
    CreateNoteRequest request = null;
    UUID authorId = AUTHOR_ID;
    // when
    CreateNote createNote = noteMapper.mapToCreateNote(request, authorId);
    // then
    assertNotNull(createNote);
    assertNotNull(createNote.authorId());
    assertNull(createNote.title());
    assertNull(createNote.content());
  }

  @Test
  void mapToCreateNote_shouldReturnOnlySetTitleAndName_whenRequestNullAndValidAuthorIdId() {
    // given
    CreateNoteRequest request = NoteDtoRequestUtils.getCreateNoteRequestBuilder().build();
    UUID authorId = null;
    // when
    CreateNote createNote = noteMapper.mapToCreateNote(request, authorId);
    // then
    assertNotNull(createNote);
    assertNull(createNote.authorId());
    assertNotNull(createNote.title());
    assertNotNull(createNote.content());
  }

  @Test
  void mapToEntity_shouldMapDomainNoteToEntity() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    // when
    NoteEntity noteEntity = noteMapper.mapToEntity(note);
    // then
    assertNotNull(noteEntity);
    assertEquals(note.getId(), noteEntity.getId());
    assertEquals(note.getAuthorId(), noteEntity.getAuthor().getId());
    assertEquals(note.getTitle(), noteEntity.getTitle());
    assertEquals(note.getContent(), noteEntity.getContent());
    assertEquals(note.getCreated(), noteEntity.getCreated());
    assertEquals(note.getUpdated().orElse(null), noteEntity.getUpdated());
    assertEquals(note.isPinned(), noteEntity.isPinned());
    assertEquals(note.getShares(createNote.authorId()), noteEntity.getShares());
  }

  @Test
  void mapToEntity_shouldReturnNull_whenNoteNull() {
    // given
    Note note = null;
    // when
    NoteEntity noteEntity = noteMapper.mapToEntity(note);
    // then
    assertNull(noteEntity);
  }

  @Test
  void mapToDomain_shouldMapEntityToDomainNote() {
    // given
    UUID id = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    UUID sharedUserId = UUID.randomUUID();
    OffsetDateTime created = OffsetDateTime.now().minusDays(1);
    OffsetDateTime updated = OffsetDateTime.now();
    UserEntity author = new UserEntity();
    author.setId(authorId);
    NoteEntity noteEntity = new NoteEntity();
    noteEntity.setId(id);
    noteEntity.setAuthor(author);
    noteEntity.setTitle("note-title");
    noteEntity.setContent("note-content");
    noteEntity.setCreated(created);
    noteEntity.setUpdated(updated);
    noteEntity.setPinned(true);
    // and
    NoteShareEntity shareEntity = new NoteShareEntity();
    shareEntity.setId(UUID.randomUUID());
    shareEntity.setNote(noteEntity);
    UserEntity sharedUser = new UserEntity();
    sharedUser.setId(sharedUserId);
    shareEntity.setUser(sharedUser);
    shareEntity.setPermissions(Set.of(NotePermission.READ));
    noteEntity.setShares(Set.of(shareEntity));
    // when
    Note note = noteMapper.mapToDomain(noteEntity);
    // then
    assertNotNull(note);
    assertEquals(id, note.getId());
    assertEquals(authorId, note.getAuthorId());
    assertEquals("note-title", note.getTitle());
    assertEquals("note-content", note.getContent());
    assertEquals(created, note.getCreated());
    assertEquals(Optional.of(updated), note.getUpdated());
    assertTrue(note.isPinned());
    // and
    Set<NoteShare> shares = note.getShares(authorId);
    assertNotNull(shares);
    assertEquals(1, shares.size());
    NoteShare share = shares.iterator().next();
    assertEquals(sharedUserId, share.userId());
    assertEquals(Set.of(NotePermission.READ), share.permissions());
  }

  @Test
  void mapToNoteShareResponse_shouldMapCorrectly() {
    // given
    NoteShare share = new NoteShare(UUID.randomUUID(), Set.of(NotePermission.EDIT));
    // when
    NoteShareResponse response = noteMapper.mapToNoteShareResponse(share);
    // then
    assertNotNull(response);
    assertEquals(share.userId(), response.getUserId());
    assertNull(response.getEmail());
    assertEquals(share.permissions(), response.getPermissions());
  }

  @Test
  void mapToNoteShareResponse_shouldReturnNull_whenShareNull() {
    // when
    NoteShareResponse response = noteMapper.mapToNoteShareResponse(null);
    // then
    assertNull(response);
  }

  @Test
  void mapToNoteShareResponse_shouldMapNullPermissions() {
    // given
    NoteShare share = new NoteShare(UUID.randomUUID(), null);
    // when
    NoteShareResponse response = noteMapper.mapToNoteShareResponse(share);
    // then
    assertNotNull(response);
    assertEquals(share.userId(), response.getUserId());
    assertNull(response.getEmail());
    assertNull(response.getPermissions());
  }

  @Test
  void mapToNoteShareResponse_shouldMapWithEmail() {
    // given
    UUID userId = UUID.randomUUID();
    Email email = Email.of("user@example.com");
    NoteShare share = new NoteShare(userId, Set.of(NotePermission.EDIT));
    // when
    NoteShareResponse response = noteMapper.mapToNoteShareResponse(share, email);
    // then
    assertNotNull(response);
    assertEquals(userId, response.getUserId());
    assertEquals(email.getValue(), response.getEmail());
    assertEquals(share.permissions(), response.getPermissions());
  }

  @Test
  void mapToNoteShareResponse_shouldMapWithEmail_andNullPermissions() {
    // given
    UUID userId = UUID.randomUUID();
    Email email = Email.of("user@example.com");
    NoteShare share = new NoteShare(userId, null);
    // when
    NoteShareResponse response = noteMapper.mapToNoteShareResponse(share, email);
    // then
    assertNotNull(response);
    assertEquals(userId, response.getUserId());
    assertEquals(email.getValue(), response.getEmail());
    assertNull(response.getPermissions());
  }

  @Test
  void mapToNoteShareResponse_shouldMapWithEmail_andNullNoteShare() {
    // given
    Email email = Email.of("user@example.com");
    NoteShare share = null;
    // when
    NoteShareResponse response = noteMapper.mapToNoteShareResponse(share, email);
    // then
    assertNotNull(response);
    assertNull(response.getUserId());
    assertNull(response.getPermissions());
    assertEquals(email.getValue(), response.getEmail());
  }

  @Test
  void mapToNoteShareResponse_shouldMapWithEmail_andNullEmail() {
    // given
    UUID userId = UUID.randomUUID();
    NoteShare share = new NoteShare(userId, Set.of(NotePermission.EDIT));
    Email email = null;
    // when
    NoteShareResponse response = noteMapper.mapToNoteShareResponse(share, email);
    // then
    assertNotNull(response);
    assertEquals(userId, response.getUserId());
    assertEquals(Set.of(NotePermission.EDIT), response.getPermissions());
    assertNull(response.getEmail());
  }

  @Test
  void mapToNoteShareResponse_shouldReturnNull_whenBothNull() {
    // given
    NoteShare share = null;
    Email email = null;
    // when
    NoteShareResponse response = noteMapper.mapToNoteShareResponse(share, email);
    // then
    assertNull(response);
  }

  @Test
  void mapToNoteValue_shouldReturnNull_whenNoteEntityNull() {
    // given
    NoteEntity noteEntity = null;
    // when
    NoteValue noteValue = noteMapper.mapToNoteValue(noteEntity);
    // then
    assertNull(noteValue);
  }

  @Test
  void mapToNoteValue_shouldReturnNull_whenAuthorNull() {
    // given
    NoteEntity noteEntity = new NoteEntity();
    noteEntity.setId(UUID.randomUUID());
    noteEntity.setAuthor(null);
    noteEntity.setTitle("title");
    noteEntity.setContent("content");
    noteEntity.setCreated(OffsetDateTime.now());
    noteEntity.setUpdated(OffsetDateTime.now());
    noteEntity.setPinned(false);
    // when
    NoteValue noteValue = noteMapper.mapToNoteValue(noteEntity);
    // then
    assertNotNull(noteValue);
    assertNull(noteValue.authorId());
  }

  @Test
  void mapToNoteValue_shouldReturnNull_whenAuthorIdNull() {
    // given
    NoteEntity noteEntity = new NoteEntity();
    noteEntity.setId(UUID.randomUUID());
    UserEntity author = new UserEntity();
    author.setId(null);
    noteEntity.setAuthor(author);
    noteEntity.setTitle("title");
    noteEntity.setContent("content");
    noteEntity.setCreated(OffsetDateTime.now());
    noteEntity.setUpdated(OffsetDateTime.now());
    noteEntity.setPinned(false);
    // when
    NoteValue noteValue = noteMapper.mapToNoteValue(noteEntity);
    // then
    assertNotNull(noteValue);
    assertNull(noteValue.authorId());
  }

  @Test
  void mapToNoteValue_shouldReturnNullShares_whenEntitySharesNull() {
    // given
    NoteEntity noteEntity = new NoteEntity();
    UserEntity author = new UserEntity();
    author.setId(UUID.randomUUID());
    noteEntity.setId(UUID.randomUUID());
    noteEntity.setAuthor(author);
    noteEntity.setTitle("title");
    noteEntity.setContent("content");
    noteEntity.setCreated(OffsetDateTime.now());
    noteEntity.setPinned(false);
    noteEntity.setShares(null);
    // when
    NoteValue noteValue = noteMapper.mapToNoteValue(noteEntity);
    // then
    assertNotNull(noteValue);
    assertNull(noteValue.shares());
  }

  @Test
  void mapToEntity_shouldMapSharesToShareEntities() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    NoteImpl note = new NoteImpl(createNote, noteValidator);
    UUID sharedUserId = UUID.randomUUID();
    note.setPermissions(note.getAuthorId(), sharedUserId, Set.of(NotePermission.READ));
    // when
    NoteEntity noteEntity = noteMapper.mapToEntity(note);
    // then
    assertNotNull(noteEntity);
    assertNotNull(noteEntity.getShares());
    assertEquals(1, noteEntity.getShares().size());
    NoteShareEntity shareEntity = noteEntity.getShares().iterator().next();
    assertNull(shareEntity.getId());
    assertNull(shareEntity.getNote());
    assertNotNull(shareEntity.getUser());
    assertEquals(sharedUserId, shareEntity.getUser().getId());
    assertEquals(Set.of(NotePermission.READ), shareEntity.getPermissions());
  }

  @Test
  void noteShareEntityToDomainNoteShare_shouldReturnNull_whenUserMissing() {
    // given
    NoteShareEntity entity = new NoteShareEntity();
    // when
    NoteShare mapped = noteMapper.noteShareEntityToDomainNoteShare(entity);
    // then
    assertNull(mapped);
  }

  @Test
  void noteShareToDomainNoteShareEntity_shouldReturnNull_whenShareNull() {
    // when
    NoteShareEntity entity = noteMapper.noteShareToDomainNoteShareEntity(null);
    // then
    assertNull(entity);
  }

  @Test
  void noteToUserEntity_shouldReturnNull_whenNoteNull() {
    // when
    UserEntity userEntity = noteMapper.noteToUserEntity(null);
    // then
    assertNull(userEntity);
  }

  @Test
  void noteStyleDTOToNoteStyle_shouldMapCorrectly_whenNoteStyleDTONotNull() {
    // given
    NoteStyleDTO noteStyleDTO = NoteStyleDTO.builder().color("#AABBCC").build();
    // when
    NoteStyle noteStyle = noteMapper.noteStyleDTOToNoteStyle(noteStyleDTO);
    // then
    assertNotNull(noteStyle);
    assertEquals(noteStyleDTO.getColor(), noteStyle.color());
  }

  @Test
  void noteStyleToNoteStyleDTO_shouldMapCorrectly_whenNoteStyleNotNull() {
    // given
    NoteStyle noteStyle = NoteStyle.builder().color("#112233").build();
    // when
    NoteStyleDTO noteStyleDTO = noteMapper.noteStyleToNoteStyleDTO(noteStyle);
    // then
    assertNotNull(noteStyleDTO);
    assertEquals(noteStyle.color(), noteStyleDTO.getColor());
  }

  @Test
  void noteShareEntityToDomainNoteShare_shouldReturnNull_whenUserIdNull() {
    // given
    NoteShareEntity entity = new NoteShareEntity();
    UserEntity user = new UserEntity();
    user.setId(null);
    entity.setUser(user);
    entity.setPermissions(Set.of(NotePermission.READ));
    // when
    NoteShare mapped = noteMapper.noteShareEntityToDomainNoteShare(entity);
    // then
    assertNull(mapped);
  }

  @Test
  void noteShareEntityToDomainNoteShare_shouldReturnNull_whenEntityNull() {
    // when
    NoteShare mapped = noteMapper.noteShareEntityToDomainNoteShare(null);
    // then
    assertNull(mapped);
  }

  @Test
  void noteSharesToDomainNoteShareEntities_shouldReturnNull_whenSharesNull() {
    // when
    Set<NoteShareEntity> entities = noteMapper.noteSharesToDomainNoteShareEntities(null);
    // then
    assertNull(entities);
  }

  @Test
  void userIdToUserEntity_shouldReturnNull_whenUserIdNull() {
    // when
    UserEntity userEntity = noteMapper.userIdToUserEntity(null);
    // then
    assertNull(userEntity);
  }

  @Test
  void userIdToUserEntity_shouldCreateUserEntity_whenUserIdNotNull() {
    // given
    UUID userId = UUID.randomUUID();
    // when
    UserEntity userEntity = noteMapper.userIdToUserEntity(userId);
    // then
    assertNotNull(userEntity);
    assertEquals(userId, userEntity.getId());
  }

  @Test
  void noteShareEntityToDomainNoteShare_shouldMapCorrectly_whenValid() {
    // given
    UUID userId = UUID.randomUUID();
    UserEntity user = new UserEntity();
    user.setId(userId);
    NoteShareEntity entity = new NoteShareEntity();
    entity.setUser(user);
    entity.setPermissions(Set.of(NotePermission.READ, NotePermission.EDIT));
    // when
    NoteShare mapped = noteMapper.noteShareEntityToDomainNoteShare(entity);
    // then
    assertNotNull(mapped);
    assertEquals(userId, mapped.userId());
    assertEquals(Set.of(NotePermission.READ, NotePermission.EDIT), mapped.permissions());
  }

  @Test
  void noteSharesToDomainNoteShareEntities_shouldMapCorrectly_whenSharesNotNull() {
    // given
    NoteShare share = new NoteShare(UUID.randomUUID(), Set.of(NotePermission.READ));
    // when
    Set<NoteShareEntity> entities = noteMapper.noteSharesToDomainNoteShareEntities(Set.of(share));
    // then
    assertNotNull(entities);
    assertEquals(1, entities.size());
  }

  @Test
  void mapToNoteResponse_shouldMapCorrectly_whenValueAndSharesNotNull() {
    // given
    UUID userId = UUID.randomUUID();
    NoteShare share = new NoteShare(userId, Set.of(NotePermission.READ));
    NoteValue value =
        NoteTestUtils.createNoteValueBuilder()
            .updated(Optional.empty())
            .shares(Set.of(share))
            .build();
    Email email = Email.of("user@example.com");
    NoteShareResponse shareResponse = noteMapper.mapToNoteShareResponse(share, email);
    // when
    NoteResponse noteResponse = noteMapper.mapToNoteResponse(value, List.of(shareResponse));
    // then
    assertNotNull(noteResponse);
    assertNotNull(noteResponse.getShares());
    assertFalse(noteResponse.getShares().isEmpty());
  }

  @Test
  void notePublicShareToEntity_shouldMapCorrectly() {
    // given
    UUID publicShareId = UUID.randomUUID();
    Set<NotePermission> permissions = Set.of(NotePermission.READ);
    NotePublicShare value = new NotePublicShare(publicShareId, permissions);

    // when
    NotePublicShareEntity entity = noteMapper.notePublicShareToEntity(value);

    // then
    assertNotNull(entity);
    assertEquals(publicShareId, entity.getId());
    assertEquals(permissions, entity.getPermissions());
    assertNull(entity.getNote());
  }

  @Test
  void notePublicShareToEntity_shouldReturnNull_whenValueIsNull() {
    // given
    NotePublicShare value = null;

    // when
    NotePublicShareEntity entity = noteMapper.notePublicShareToEntity(value);

    // then
    assertNull(entity);
  }

  @Test
  void entityToNotePublicShare_shouldMapCorrectly() {
    // given
    UUID publicShareId = UUID.randomUUID();
    NotePublicShareEntity entity = new NotePublicShareEntity();
    entity.setId(publicShareId);

    // when
    NotePublicShare value = noteMapper.entityToNotePublicShare(entity);

    // then
    assertNotNull(value);
    assertEquals(publicShareId, value.publicShareId());
  }

  @Test
  void entityToNotePublicShare_shouldReturnNull_whenEntityIsNull() {
    // given
    NotePublicShareEntity entity = null;

    // when
    NotePublicShare value = noteMapper.entityToNotePublicShare(entity);

    // then
    assertNull(value);
  }

  @Test
  void mapToNotePublicShareResponse_shouldMapPermissions() {
    // given
    UUID publicShareId = UUID.randomUUID();
    Set<NotePermission> permissions = Set.of(NotePermission.READ, NotePermission.EDIT);
    NotePublicShare notePublicShare = new NotePublicShare(publicShareId, permissions);

    // when
    NotePublicShareResponse response = noteMapper.mapToNotePublicShareResponse(notePublicShare);

    // then
    assertNotNull(response);
    assertEquals(publicShareId, response.getPublicShareId());
    assertEquals(permissions, response.getPermissions());
  }

  @Test
  void mapToNotePublicShareResponse_shouldReturnNull_whenNotePublicShareIsNull() {
    // given
    NotePublicShare notePublicShare = null;

    // when
    NotePublicShareResponse response = noteMapper.mapToNotePublicShareResponse(notePublicShare);

    // then
    assertNull(response);
  }

  @Test
  void mapToNotePublicShareResponse_shouldReturnNull_whenOptionalIsEmpty() {
    // given
    Optional<NotePublicShare> publicShare = Optional.empty();

    // when
    NotePublicShareResponse response = noteMapper.mapToNotePublicShareResponse(publicShare);

    // then
    assertNull(response);
  }

  @Test
  void mapToNotePublicShareResponse_shouldMapPermissions_whenOptionalIsPresent() {
    // given
    UUID publicShareId = UUID.randomUUID();
    Set<NotePermission> permissions = Set.of(NotePermission.READ);
    NotePublicShare notePublicShare = new NotePublicShare(publicShareId, permissions);
    Optional<NotePublicShare> publicShare = Optional.of(notePublicShare);

    // when
    NotePublicShareResponse response = noteMapper.mapToNotePublicShareResponse(publicShare);

    // then
    assertNotNull(response);
    assertEquals(publicShareId, response.getPublicShareId());
    assertEquals(permissions, response.getPermissions());
  }
}
