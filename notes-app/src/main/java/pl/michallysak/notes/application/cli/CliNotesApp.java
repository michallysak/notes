package pl.michallysak.notes.application.cli;

import jakarta.annotation.Nonnull;
import pl.michallysak.notes.application.NotesApplication;
import pl.michallysak.notes.application.cli.presenter.CliNotePresenter;
import pl.michallysak.notes.application.cli.presenter.CliPresenter;
import pl.michallysak.notes.note.repository.InMemoryNoteRepository;
import pl.michallysak.notes.note.repository.NoteRepository;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.note.service.NoteServiceImpl;
import pl.michallysak.notes.note.validator.NoteValidator;
import pl.michallysak.notes.note.validator.NoteValidatorImpl;

import java.util.Arrays;
import java.util.List;


public class CliNotesApp implements NotesApplication {
    private final CliPresenter cliPresenter = new CliPresenter();
    private final CliNotePresenter cliNotePresenter;

    public CliNotesApp(String[] args) {
        cliPresenter.showln("Creating CLI Notes Application...");
        cliNotePresenter = getNotePresenter(args);
    }

    @Override
    public void start() {
        cliPresenter.showln("Starting CLI Notes Application...");
        cliNotePresenter.start();
        cliPresenter.showln("Exiting CLI Notes Application...");
    }

    private CliNotePresenter getNotePresenter(String[] args) {
        List<String> arguments = Arrays.asList(args);
        final CliNotePresenter cliNotePresenter;
        NoteValidator noteValidator = new NoteValidatorImpl();
        NoteRepository noteRepository = getNoteRepository(arguments);
        NoteService noteService = new NoteServiceImpl(noteValidator, noteRepository);
        cliNotePresenter = new CliNotePresenter(cliPresenter, noteService);
        return cliNotePresenter;
    }

    private NoteRepository getNoteRepository(List<String> arguments) {
        if (arguments.contains("--persistence=in-memory")) {
            cliPresenter.showln("Using persistence type in-memory");
            return new InMemoryNoteRepository();
        }

        cliPresenter.showln("Persistence type not provided, use in-memory");
        return new InMemoryNoteRepository();
    }

}
