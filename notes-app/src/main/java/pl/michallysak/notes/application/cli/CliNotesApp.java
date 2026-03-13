package pl.michallysak.notes.application.cli;

import pl.michallysak.notes.application.NotesApplication;
import pl.michallysak.notes.application.cli.controller.CliNoteController;
import pl.michallysak.notes.note.repository.InMemoryNoteRepository;
import pl.michallysak.notes.note.repository.NoteRepository;
import pl.michallysak.notes.note.service.NoteServiceImpl;
import pl.michallysak.notes.note.validator.NoteValidatorImpl;

import java.util.Arrays;
import java.util.List;


public class CliNotesApp implements NotesApplication {

    private final CliNoteController noteController;

    public CliNotesApp(String[] args) {
        System.out.println("Creating CLI Notes Application");
        List<String> arguments = Arrays.asList(args);
        NoteServiceImpl noteService = new NoteServiceImpl(new NoteValidatorImpl(), getNoteRepository(arguments));
        noteController = new CliNoteController(noteService);
    }

    @Override
    public void start() {
        System.out.println("Starting CLI Notes Application...");
        noteController.startNoteMenuLoop();
        System.out.println("Exiting...");
    }

    private NoteRepository getNoteRepository(List<String> arguments) {
        if (arguments.contains("--persistence=in-memory")) {
            return new InMemoryNoteRepository();
        }

        System.out.println("Persistence type not provided, use in-memory.");
        return new InMemoryNoteRepository();
    }

}
