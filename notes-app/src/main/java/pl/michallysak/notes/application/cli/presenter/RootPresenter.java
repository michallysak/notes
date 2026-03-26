package pl.michallysak.notes.application.cli.presenter;

import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.application.cli.io.IO;
import pl.michallysak.notes.application.cli.note.beans.NoteBeans;
import pl.michallysak.notes.application.cli.note.presenter.CliNotePresenter;

@RequiredArgsConstructor
public class RootPresenter implements Presenter {

  private final NoteBeans noteBeans;

  @Override
  public void present() {
    IO<String> console = noteBeans.console();
    console.println("Starting CLI Notes Application...");
    CliNotePresenter cliNotePresenter = noteBeans.notePresenter();
    cliNotePresenter.present();
    console.println("Exiting CLI Notes Application...");
  }
}
