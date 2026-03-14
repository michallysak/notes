package pl.michallysak.notes.application.cli.presenter;

import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.common.validator.TextRange;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.service.NoteService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CliNotePresenter {
    private static final TextRange TITLE_LENGTH_RANGE = TextRange.of(3, 64);
    private static final TextRange CONTENT_LENGTH_RANGE = TextRange.of(0, 2048);

    private boolean shouldContinue = true;

    private final CliPresenter cliPresenter;
    private final NoteService noteService;

    public void start() {
        while (shouldContinue) {
            printMenu();
            String option = cliPresenter.prompt("Choose option: ");
            handleOption(option);
            cliPresenter.showln();
        }
    }

    private void printMenu() {
        cliPresenter.showln("--- Notes Menu ---");
        cliPresenter.showln("1. Create note");
        cliPresenter.showln("2. List notes");
        cliPresenter.showln("3. Show note by id");
        cliPresenter.showln("4. Update note");
        cliPresenter.showln("5. Delete note");
        cliPresenter.showln("0. Exit");
    }

    private void handleOption(String option) {
        try {
            switch (option) {
                case "1" -> createNote();
                case "2" -> listNotes();
                case "3" -> getNote();
                case "4" -> updateNote();
                case "5" -> deleteNote();
                case "0" -> shouldContinue = false;
                default -> cliPresenter.showln("Invalid option. Try again.");
            }
        } catch (Exception e) {
            cliPresenter.showln("Error: " + e.getMessage());
        }
    }

    private void createNote() {
        String title = promptTitle();
        String content = promptContent();
        CreateNote createNote = new CreateNote(title, content);
        NoteValue note = noteService.createNote(createNote);
        cliPresenter.showln("Created: " + note);
    }

    private void listNotes() {
        List<NoteValue> notes = noteService.getCreatedNotes();
        if (notes.isEmpty()) {
            cliPresenter.showln("No notes found.");
        } else {
            notes.stream().map(NoteValue::toString).forEach(cliPresenter::showln);
        }
    }

    private void getNote() {
        UUID id = promptForId();
        NoteValue note = noteService.getCreatedNote(id);
        cliPresenter.showln(note.toString());
    }

    private void updateNote() {
        UUID id = promptForId();
        String title = promptTitle();
        String content = promptContent();
        boolean pinned = promptPinned();
        NoteUpdate noteUpdate = new NoteUpdate(title, content, pinned);
        NoteValue note = noteService.updateNote(id, noteUpdate);
        cliPresenter.showln("Updated: " + note);

    }

    private void deleteNote() {
        UUID id = promptForId();
        noteService.deleteNote(id);
        cliPresenter.showln("Deleted note with id: " + id);
    }

    private String promptTitle() {
        return cliPresenter.prompt("Title %s: ".formatted(TITLE_LENGTH_RANGE));
    }

    private String promptContent() {
        return cliPresenter.prompt("Content %s: ".formatted(CONTENT_LENGTH_RANGE));
    }

    private UUID promptForId() {
        String idStr = cliPresenter.prompt("Note id: ");
        return UUID.fromString(idStr);
    }

    private boolean promptPinned() {
        String pinnedStr = cliPresenter.prompt("Pinned (true/false): ");
        return Boolean.parseBoolean(pinnedStr);
    }
}
