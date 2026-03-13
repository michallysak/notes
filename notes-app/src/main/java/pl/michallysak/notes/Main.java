package pl.michallysak.notes;

import pl.michallysak.notes.application.NotesApplication;
import pl.michallysak.notes.application.cli.CliNotesApp;

public class Main {
    public static void main(String[] args) {
        createNoteApp(args).start();
    }

    private static NotesApplication createNoteApp(String[] args) {
        if (args.length > 0 && "--cli".equalsIgnoreCase(args[0])) {
            return new CliNotesApp(args);
        }
        System.out.println("Usage: java -jar notes-app.jar [--cli]");
        System.exit(1);
        throw new IllegalStateException();
    }
}
