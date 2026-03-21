package pl.michallysak.notes.application.cli.note.presenter;

import pl.michallysak.notes.application.cli.io.IO;
import pl.michallysak.notes.application.cli.presenter.Presenter;
import pl.michallysak.notes.common.validator.TextRange;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.user.service.CurrentUserProvider;

import java.util.List;
import java.util.UUID;

public class CliNotePresenter implements Presenter {
    private final static TextRange TITLE_LENGTH_RANGE = TextRange.of(3, 64);
    private final static TextRange CONTENT_LENGTH_RANGE = TextRange.of(0, 2048);

    private boolean shouldContinue = true;

    private final IO<String> io;
    private final NoteService noteService;
    private final UUID authorId;

    public CliNotePresenter(IO<String> io, NoteService noteService, CurrentUserProvider currentUserProvider) {
        this.io = io;
        this.noteService = noteService;
        this.authorId = currentUserProvider.getCurrentUserId();
    }

    @Override
    public void present() {
        while (shouldContinue) {
            printMenu();
            String option = io.readLine("Choose option: ");
            handleOption(option);
            io.println("");
        }
    }

    private void printMenu() {
        io.println("--- Notes Menu ---");
        io.println(NoteOption.CREATE.getValue() + ". Create note");
        io.println(NoteOption.LIST.getValue() + ". List notes");
        io.println(NoteOption.SHOW.getValue() + ". Show note by id");
        io.println(NoteOption.UPDATE.getValue() + ". Update note");
        io.println(NoteOption.DELETE.getValue() + ". Delete note");
        io.println(NoteOption.EXIT.getValue() + ". Exit");
    }

    private void handleOption(String option) {
        try {
            NoteOption noteOption = NoteOption.fromValue(option);
            if (noteOption == null) {
                io.println("Invalid option. Try again.");
                return;
            }
            switch (noteOption) {
                case CREATE -> createNote();
                case LIST -> listNotes();
                case SHOW -> getNote();
                case UPDATE -> updateNote();
                case DELETE -> deleteNote();
                case EXIT -> shouldContinue = false;
            }
        } catch (Exception e) {
            io.println("Error: " + e.getMessage());
        }
    }

    private void createNote() {
        CreateNote createNote = new CreateNote(promptTitle(), promptContent(), authorId);
        NoteValue note = noteService.createNote(createNote);
        io.println("Created: " + note);
    }

    private void listNotes() {
        List<NoteValue> notes = noteService.getCreatedNotes(authorId);
        if (notes.isEmpty()) {
            io.println("No notes found.");
        } else {
            notes.stream().map(NoteValue::toString).forEach(io::println);
        }
    }

    private void getNote() {
        UUID id = promptId();
        NoteValue note = noteService.getCreatedNote(id, authorId);
        io.println(note.toString());
    }

    private void updateNote() {
        UUID id = promptId();
        NoteUpdate noteUpdate = NoteUpdate.builder()
                .title(promptTitle())
                .content(promptContent())
                .pinned(promptPinned())
                .actingUserId(authorId)
                .build();
        NoteValue note = noteService.updateNote(id, noteUpdate);
        io.println("Updated: " + note);
    }

    private void deleteNote() {
        UUID id = promptId();
        noteService.deleteNote(id, authorId);
        io.println("Deleted note with id: " + id);
    }

    private String nullIfBlankWithNotice(String s, String fieldName) {
        if(s.isBlank()) {
            printNoChange(fieldName);
            return null;
        }
        return s;
    }

    private void printNoChange(String b) {
        io.println("No change will be applied to " + b + ".");
    }

    private UUID promptId() {
        String idStr = io.readLine("Note id: ");
        return UUID.fromString(idStr);
    }

    private Boolean promptPinned() {
        String pinnedStr = io.readLine("Pinned [y/n] (default no-change): ");
        return switch (pinnedStr) {
            case "y", "Y" -> true;
            case "n", "N" -> false;
            default -> {
                printNoChange("pinned status");
                yield null;
            }
        };
    }

    private String promptTitle() {
        String message = "Title %s: ".formatted(TITLE_LENGTH_RANGE);
        return nullIfBlankWithNotice(io.readLine(message), "title");
    }

    private String promptContent() {
        String message = "Content %s: ".formatted(CONTENT_LENGTH_RANGE);
        return nullIfBlankWithNotice(io.readLine(message), "content");
    }
}
