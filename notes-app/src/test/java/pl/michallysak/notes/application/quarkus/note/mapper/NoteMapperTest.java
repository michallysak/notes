package pl.michallysak.notes.application.quarkus.note.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.Optional;
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
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;
import pl.michallysak.notes.application.quarkus.note.persistence.NoteEntity;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.validator.NoteValidator;
import pl.michallysak.notes.user.repository.UserEntity;

@ExtendWith(MockitoExtension.class)
class NoteMapperTest {

  private static final UUID AUTHOR_ID = UUID.randomUUID();

  @Mock private NoteValidator noteValidator;
  private NoteMapper noteMapper;

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
    NoteValue value = NoteTestUtils.createNoteValueBuilder().updated(updated).build();
    // when
    NoteResponse noteResponse = noteMapper.mapToNoteResponse(value);
    // then
    assertEquals(value.id(), noteResponse.getId());
    assertEquals(value.title(), noteResponse.getTitle());
    assertEquals(value.content(), noteResponse.getContent());
    assertEquals(value.created(), noteResponse.getCreated());
    assertEquals(value.pinned(), noteResponse.isPinned());
    assertEquals(updated.orElse(null), noteResponse.getUpdated());
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
}
