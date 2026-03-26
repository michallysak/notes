package pl.michallysak.notes.application.quarkus;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.NotesApplication;

class QuarkusNotesAppTest {
  @Test
  void constructor_shouldCreateNotesApplicationInstance() {
    // when
    QuarkusNotesApp app = new QuarkusNotesApp();
    // then
    assertInstanceOf(NotesApplication.class, app);
  }
}
