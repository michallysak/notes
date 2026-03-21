package pl.michallysak.notes.note.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoteAccessExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        // given
        UUID noteId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        // when
        NoteAccessException noteAccessException = new NoteAccessException(noteId, userId);
        // then
        String expected = "User %s is not the author of note %s".formatted(userId, noteId);
        assertEquals(expected, noteAccessException.getMessage());
    }
}

