package pl.michallysak.notes.application.cli;

import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.NotesApplication;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CliNotesAppTest {

    @Test
    void constructor_shouldCreateNotesApplicationInstance() {
        // when
        CliNotesApp cliNotesApp = new CliNotesApp(new String[]{});
        // then
        assertInstanceOf(NotesApplication.class, cliNotesApp);
    }

}
