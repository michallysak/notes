package pl.michallysak.notes.application.quarkus;

import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.NotesApplication;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class QuarkusNotesAppTest {
    @Test
    void constructor_shouldCreateNotesApplicationInstance() {
        // when
        QuarkusNotesApp app = new QuarkusNotesApp();
        // then
        assertInstanceOf(NotesApplication.class, app);
    }
}

