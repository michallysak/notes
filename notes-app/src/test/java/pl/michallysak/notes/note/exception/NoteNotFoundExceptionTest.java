package pl.michallysak.notes.note.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoteNotFoundExceptionTest {

    @Test
    void constructor_shouldSetMessage() {
        // when
        NoteNotFoundException exception = new NoteNotFoundException();
        // then
        assertEquals("Note not found", exception.getMessage());
    }

}

