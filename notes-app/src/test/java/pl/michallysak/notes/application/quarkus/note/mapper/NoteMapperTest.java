package pl.michallysak.notes.application.quarkus.note.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;

class NoteMapperTest {

  private static final UUID AUTHOR_ID = UUID.randomUUID();
  private final NoteMapper noteMapper = new NoteMapperImpl();

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
}
