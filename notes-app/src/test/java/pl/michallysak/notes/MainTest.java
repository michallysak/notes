package pl.michallysak.notes;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.michallysak.notes.application.cli.CliNotesApp;
import pl.michallysak.notes.application.quarkus.QuarkusNotesApp;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MainTest {
    @Test
    void main_shouldStartCliNotesApp_whenCliArg() {
        try (var mocked = Mockito.mockConstruction(CliNotesApp.class, (mock, context) -> {
            Mockito.doNothing().when(mock).start();
        })) {
            Main.main(new String[]{"--cli"});
            Mockito.verify(mocked.constructed().getFirst()).start();
        }
    }

    @Test
    void main_shouldStartQuarkusNotesApp_whenQuarkusArg() {
        try (var mocked = Mockito.mockConstruction(QuarkusNotesApp.class, (mock, context) -> {
            Mockito.doNothing().when(mock).start();
        })) {
            Main.main(new String[]{"--quarkus"});
            Mockito.verify(mocked.constructed().getFirst()).start();
        }

    }
    @Test
    void main_shouldExit_whenUnknownArg() {
        assertThrows(IllegalArgumentException.class, () -> Main.createNoteApp(new String[]{"--unknown"}));
    }

    @Test
    void main_shouldExit_whenNoArg() {
        assertThrows(IllegalArgumentException.class, () -> Main.createNoteApp(new String[]{}));
    }
}
