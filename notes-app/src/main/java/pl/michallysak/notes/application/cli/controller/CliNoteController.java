package pl.michallysak.notes.application.cli.controller;

import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.common.validator.TextRange;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.service.NoteService;

import java.util.List;
import java.util.Scanner;

@RequiredArgsConstructor
public class CliNoteController {
    private final static TextRange TITLE_LENGTH_RANGE = TextRange.of(3, 64);
    private final static TextRange CONTENT_LENGTH_RANGE = TextRange.of(0, 2048);

    private final NoteService noteService;

    public void startNoteMenuLoop() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            printMenu();
            String input = scanner.nextLine();
            switch (input) {
                case "1" -> createNote(scanner);
                case "2" -> listNotes();
                case "3" -> getNote(scanner);
                case "4" -> updateNote(scanner);
                case "5" -> deleteNote(scanner);
                case "0" -> {
                    return;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n--- Notes Menu ---");
        System.out.println("1. Create note");
        System.out.println("2. List notes");
        System.out.println("3. Show note by ID");
        System.out.println("4. Update note");
        System.out.println("5. Delete note");
        System.out.println("0. Exit");
        System.out.print("Choose option: ");
    }

    private void createNote(Scanner scanner) {
        System.out.print("Title " + TITLE_LENGTH_RANGE + ": ");
        String title = scanner.nextLine();
        System.out.print("Content " + CONTENT_LENGTH_RANGE + ": ");
        String content = scanner.nextLine();
        try {
            NoteValue note = noteService.createNote(new CreateNote(title, content));
            System.out.println("Created: " + note);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listNotes() {
        List<NoteValue> notes = noteService.getCreatedNotes();
        if (notes.isEmpty()) {
            System.out.println("No notes found.");
        } else {
            notes.forEach(System.out::println);
        }
    }

    private void getNote(Scanner scanner) {
        System.out.print("Note ID: ");
        String id = scanner.nextLine();
        try {
            NoteValue note = noteService.getCreatedNote(java.util.UUID.fromString(id));
            System.out.println(note);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateNote(Scanner scanner) {
        System.out.print("Note ID: ");
        String id = scanner.nextLine();
        System.out.print("New Title " + TITLE_LENGTH_RANGE + ": ");
        String title = scanner.nextLine();
        System.out.print("New Content " + CONTENT_LENGTH_RANGE + ": ");
        String content = scanner.nextLine();
        System.out.print("Pinned (true/false): ");
        String pinnedStr = scanner.nextLine();
        boolean pinned = Boolean.parseBoolean(pinnedStr);
        try {
            NoteUpdate noteUpdate = new NoteUpdate(title, content, pinned);
            NoteValue note = noteService.updateNote(java.util.UUID.fromString(id), noteUpdate);
            System.out.println("Updated: " + note);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void deleteNote(Scanner scanner) {
        System.out.print("Note ID: ");
        String id = scanner.nextLine();
        try {
            noteService.deleteNote(java.util.UUID.fromString(id));
            System.out.println("Deleted.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
