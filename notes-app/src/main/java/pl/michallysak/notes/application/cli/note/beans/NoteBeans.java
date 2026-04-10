package pl.michallysak.notes.application.cli.note.beans;

import java.util.Arrays;
import java.util.List;
import pl.michallysak.notes.application.cli.io.ConsoleIO;
import pl.michallysak.notes.application.cli.io.IO;
import pl.michallysak.notes.application.cli.note.presenter.CliNotePresenter;
import pl.michallysak.notes.application.cli.presenter.Presenter;
import pl.michallysak.notes.application.cli.presenter.RootPresenter;
import pl.michallysak.notes.note.domain.event.DomainEventPublisher;
import pl.michallysak.notes.note.repository.InMemoryNoteRepository;
import pl.michallysak.notes.note.repository.NoteRepository;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.note.service.NoteServiceImpl;
import pl.michallysak.notes.user.service.CurrentUserProvider;
import pl.michallysak.notes.user.service.NoAuthCurrentUserProvider;

public class NoteBeans {
  private final List<String> arguments;
  private final IO<String> io;
  private NoteService noteServiceInstance;
  private DomainEventPublisher domainEventPublisher;
  private CliNotePresenter notePresenterInstance;
  private NoteRepository noteRepositoryInstance;
  private Presenter rootPresenterInstance;
  private CurrentUserProvider currentUserProvider;

  public NoteBeans(String[] arguments) {
    this.arguments = Arrays.asList(arguments);
    this.io = new ConsoleIO();
  }

  public IO<String> console() {
    return io;
  }

  public CurrentUserProvider currentUserProvider() {
    if (currentUserProvider == null) {
      currentUserProvider = new NoAuthCurrentUserProvider();
    }
    return currentUserProvider;
  }

  public Presenter rootPresenter() {
    if (rootPresenterInstance == null) {
      rootPresenterInstance = new RootPresenter(this);
    }
    return rootPresenterInstance;
  }

  public CliNotePresenter notePresenter() {
    if (notePresenterInstance == null) {
      notePresenterInstance = new CliNotePresenter(io, noteService(), currentUserProvider());
    }
    return notePresenterInstance;
  }

  public DomainEventPublisher domainEventPublisher() {
    if (domainEventPublisher == null) {
      domainEventPublisher = events -> {};
    }
    return domainEventPublisher;
  }

  public NoteService noteService() {
    if (noteServiceInstance == null) {
      noteServiceInstance = new NoteServiceImpl(noteRepository(), domainEventPublisher());
    }
    return noteServiceInstance;
  }

  public NoteRepository noteRepository() {
    if (noteRepositoryInstance == null) {
      if (this.arguments.contains("--persistence=in-memory")) {
        io.println("Using persistence type in-memory");
        noteRepositoryInstance = new InMemoryNoteRepository();
      } else {
        io.println("Persistence type not provided, use in-memory");
        noteRepositoryInstance = new InMemoryNoteRepository();
      }
    }
    return noteRepositoryInstance;
  }
}
