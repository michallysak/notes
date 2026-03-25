package pl.michallysak.notes;

import pl.michallysak.notes.application.NotesApplication;
import pl.michallysak.notes.application.cli.CliNotesApp;
import pl.michallysak.notes.application.quarkus.QuarkusNotesApp;

public class Main {
  public static void main(String[] args) {
    try {
      createNoteApp(args).start();
    } catch (IllegalArgumentException e) {
      System.out.println("Usage: java -jar notes-app.jar [--cli|--quarkus]");
      System.exit(1);
    }
  }

  static NotesApplication createNoteApp(String[] args) {
    if (args.length == 0) {
      throw new IllegalArgumentException("No arguments provided");
    }
    if ("--cli".equalsIgnoreCase(args[0])) {
      return new CliNotesApp(args);
    }
    if ("--quarkus".equalsIgnoreCase(args[0])) {
      return new QuarkusNotesApp();
    }
    throw new IllegalArgumentException("Unknown argument: " + args[0]);
  }
}
