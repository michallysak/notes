package pl.michallysak.notes.application.quarkus.note.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils.createNoteUpdateRequestBuilder;
import static pl.michallysak.notes.application.quarkus.note.dto.NoteDtoRequestUtils.getCreateNoteRequestBuilder;
import static pl.michallysak.notes.application.quarkus.note.resource.NoteResourceTestClient.*;
import static pl.michallysak.notes.helpers.TestExtensions.toJsonString;

@QuarkusTest
class NoteResourceIT {

    @BeforeEach
    void setUp() {
        Response response = getNotes();
        Arrays.stream(response.as(NoteResponse[].class))
                .map(NoteResponse::getId)
                .forEach(id -> deleteNote(id.toString()));
    }

    @Test
    void createNote_shouldReturn201AndLocationAndBody() {
        // given
        CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
        // when
        Response response = createNote(toJsonString(createNoteRequest));
        // then
        response.then().statusCode(201);
        // and
        NoteResponse noteResponse = response.as(NoteResponse.class);
        assertEquals(createNoteRequest.getTitle(), noteResponse.getTitle());
        assertEquals(createNoteRequest.getContent(), noteResponse.getContent());
        assertNotNull(noteResponse.getId());
        assertNotNull(noteResponse.getCreated());
        assertNull(noteResponse.getUpdated());
        assertFalse(noteResponse.isPinned());
    }

    @Test
    void createNote_shouldReturn400_whenValidationException() {
        // given
        CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().title(null).build();
        // when
        Response response = createNote(toJsonString(createNoteRequest));
        // then
        response.then().statusCode(400);
        assertTrue(response.asString().contains("Title cannot be null"));
    }

    @Test
    void getNotes_shouldReturnListWithCreatedNote() {
        // given
        CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
        String noteId = createNote(toJsonString(createNoteRequest)).then()
                .statusCode(201)
                .extract().path("id");
        // when
        Response response = getNotes();
        // then
        response.then().statusCode(200);
        NoteResponse[] noteResponses = response.as(NoteResponse[].class);
        assertEquals(1, noteResponses.length);
        // and
        NoteResponse noteResponse = noteResponses[0];
        assertEquals(noteId, noteResponse.getId().toString());
        assertEquals(createNoteRequest.getTitle(), noteResponse.getTitle());
        assertEquals(createNoteRequest.getContent(), noteResponse.getContent());
        assertNotNull(noteResponse.getCreated());
        assertNull(noteResponse.getUpdated());
        assertFalse(noteResponse.isPinned());
    }

    @Test
    void getNote_shouldReturn200AndNote() {
        // given
        CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
        String noteId = createNote(toJsonString(createNoteRequest)).then()
                .statusCode(201)
                .extract().path("id");
        // when
        Response response = getNote(noteId);
        // then
        response.then().statusCode(200);
        // and
        NoteResponse note = response.as(NoteResponse.class);
        assertEquals(noteId, note.getId().toString());
        assertEquals(createNoteRequest.getTitle(), note.getTitle());
        assertEquals(createNoteRequest.getContent(), note.getContent());
        assertNotNull(note.getCreated());
        assertNull(note.getUpdated());
    }

    @Test
    void getNote_shouldReturn404_whenNotExists() {
        // given
        String nonExistentId = UUID.randomUUID().toString();
        // when
        Response response = getNote(nonExistentId);
        // then
        response.then().statusCode(404);
    }

    @Test
    void updateNote_shouldReturn200AndUpdatedBody() {
        // given
        CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
        String noteId = createNote(toJsonString(createNoteRequest)).then().statusCode(201).extract().path("id");
        NoteUpdateRequest noteUpdateRequest = createNoteUpdateRequestBuilder()
                .title("newTitle")
                .content("newContent")
                .pinned(true)
                .build();
        // when
        Response response = updateNote(noteId, toJsonString(noteUpdateRequest));
        // then
        response.then().statusCode(200);
        // and
        NoteResponse noteResponse = response.as(NoteResponse.class);
        assertEquals(noteUpdateRequest.getTitle(), noteResponse.getTitle());
        assertEquals(noteUpdateRequest.getContent(), noteResponse.getContent());
        assertNotNull(noteResponse.getId());
        assertNotNull(noteResponse.getCreated());
        assertNotNull(noteResponse.getUpdated());
        assertTrue(noteResponse.isPinned());
    }

    @Test
    void updateNote_shouldReturn400_whenValidationException() {
        // given
        CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
        String noteId = createNote(toJsonString(createNoteRequest)).then()
                .statusCode(201)
                .extract().path("id");
        NoteUpdateRequest noteUpdateRequest = createNoteUpdateRequestBuilder().title("X").build();
        // when
        Response response = updateNote(noteId, toJsonString(noteUpdateRequest));
        // then
        response.then().statusCode(400);
        assertTrue(response.asString().contains("Title not meet length requirements"));
    }

    @Test
    void deleteNote_shouldReturn200AndDelete() {
        // given
        CreateNoteRequest createNoteRequest = getCreateNoteRequestBuilder().build();
        String noteId = createNote(toJsonString(createNoteRequest)).then()
                .statusCode(201)
                .extract().path("id");
        // when
        Response deleteResponse = deleteNote(noteId);
        // then
        deleteResponse.then().statusCode(204);
    }

    @Test
    void deleteNote_shouldReturn404_whenNotExists() {
        // given
        String nonExistentId = UUID.randomUUID().toString();
        // when
        Response response = deleteNote(nonExistentId);
        // then
        response.then().statusCode(404);
    }

}
