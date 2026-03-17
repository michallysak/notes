package pl.michallysak.notes.application.quarkus;

import io.quarkus.runtime.Quarkus;
import pl.michallysak.notes.application.NotesApplication;


public class QuarkusNotesApp implements NotesApplication {
    @Override
    public void start() {
        Quarkus.run();
    }
}

