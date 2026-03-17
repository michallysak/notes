package pl.michallysak.notes.application.quarkus.note.resource;

import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;

import static io.restassured.RestAssured.given;

public class NoteResourceTestClient {

    private static final String NOTES_PATH = "/notes";

    private static String getNotePath(String noteId) {
        return NOTES_PATH + "/" + noteId;
    }

    public static Response createNote(String bodyJson) {
        return given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bodyJson)
                .when()
                .post(NOTES_PATH);
    }

    public static Response updateNote(String noteId, String bodyJson) {
        return given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(bodyJson)
                .when()
                .put(getNotePath(noteId));
    }

    public static Response getNote(String noteId) {
        return given()
                .when()
                .get(getNotePath(noteId));
    }

    public static Response getNotes() {
        return given()
                .when()
                .get(NOTES_PATH);
    }

    public static Response deleteNote(String noteId) {
        return given()
                .when()
                .delete(getNotePath(noteId));
    }

}
