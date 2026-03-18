package pl.michallysak.notes.application.quarkus.note.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class NoteMapperTest {

    private final static UUID AUTHOR_ID = UUID.randomUUID();
    private final NoteMapper noteMapper = new NoteMapperImpl();

    @Test
    void toNoteUpdate_shouldMapCorrectly() {
        // given
        NoteUpdateRequest noteUpdateRequest = NoteDtoRequestUtils.createNoteUpdateRequestBuilder().build();
        // when
        NoteUpdate noteUpdate = noteMapper.mapToNoteUpdate(noteUpdateRequest);
        // then
        assertEquals(noteUpdateRequest.getTitle(), noteUpdate.title());
        assertEquals(noteUpdateRequest.getContent(), noteUpdate.content());
        assertEquals(noteUpdateRequest.getPinned(), noteUpdate.pinned());
    }

    @Test
    void toNoteUpdate_shouldMapCorrectly_whenNull() {
        // given
        NoteUpdateRequest value = null;
        // when
        NoteUpdate noteResponse = noteMapper.mapToNoteUpdate(value);
        // then
        assertNull(noteResponse);
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
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<OffsetDateTime> updated
    ) {
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
                Arguments.of(Optional.empty()),
                Arguments.of(Optional.of(OffsetDateTime.now()))
        );
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
