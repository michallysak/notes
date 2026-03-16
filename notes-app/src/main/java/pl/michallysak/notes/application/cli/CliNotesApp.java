package pl.michallysak.notes.application.cli;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.application.NotesApplication;
import pl.michallysak.notes.application.cli.note.beans.NoteBeans;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CliNotesApp implements NotesApplication {

    private final NoteBeans noteBeans;

    public CliNotesApp(String[] args) {
        noteBeans = new NoteBeans(args);
    }

    @Override
    public void start() {
        noteBeans.rootPresenter().present();
    }

}
