package pl.michallysak.notes.application.cli.note.presenter;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import pl.michallysak.notes.application.cli.io.TestTextIO;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.service.NoteService;

@Getter
public class CliNotePresenterTestClient {
    private final TestTextIO io;
    private final CliNotePresenter presenter;

    public CliNotePresenterTestClient(NoteService noteService) {
        this.io = TestTextIO.create();
        this.presenter = new CliNotePresenter(io, noteService);
    }

    public String getOutput() {
        return io.toString();
    }

    public CliNotePresenterTestClient present(String... options) {
        io.addInputs(List.of(options));
        presenter.present();
        return this;
    }

    public CliNotePresenterTestClient createNote(CreateNote createNote) {
        io.addInputs(List.of(
            NoteOption.CREATE.getValue(),
            createNote.title(),
            createNote.content(),
            NoteOption.EXIT.getValue()
        ));
        presenter.present();
        return this;
    }

    public CliNotePresenterTestClient listNotes() {
        io.addInputs(List.of(NoteOption.LIST.getValue(), NoteOption.EXIT.getValue()));
        presenter.present();
        return this;
    }

    public CliNotePresenterTestClient showNote(String noteId) {
        io.addInputs(List.of(NoteOption.SHOW.getValue(), noteId, NoteOption.EXIT.getValue()));
        presenter.present();
        return this;
    }

    public CliNotePresenterTestClient updateNote(String noteId, NoteUpdate noteUpdate) {
        io.addInputs(Arrays.asList(
            NoteOption.UPDATE.getValue(),
            noteId,
            noteUpdate.title(),
            noteUpdate.content(),
            noteUpdate.pinned() ? "y" : "n",
            NoteOption.EXIT.getValue()
        ));
        presenter.present();
        return this;
    }

    public CliNotePresenterTestClient deleteNote(String noteId) {
        io.addInputs(List.of(NoteOption.DELETE.getValue(), noteId, NoteOption.EXIT.getValue()));
        presenter.present();
        return this;
    }

    public String createNoteAndGetId(CreateNote createNote) {
        createNote(createNote);
        return getLastNoteIdFromOutput();
    }

    public String getLastNoteIdFromOutput() {
        String output = getOutput();
        return output.replaceAll("(?s).*id=(\\w+-\\w+-\\w+-\\w+-\\w+).*", "$1");
    }

}
