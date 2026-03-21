package pl.michallysak.notes.application.cli.note.beans;

import org.junit.jupiter.api.Test;
import pl.michallysak.notes.application.cli.io.ConsoleIO;
import pl.michallysak.notes.application.cli.note.presenter.CliNotePresenter;
import pl.michallysak.notes.application.cli.presenter.Presenter;
import pl.michallysak.notes.application.cli.presenter.RootPresenter;
import pl.michallysak.notes.note.repository.InMemoryNoteRepository;
import pl.michallysak.notes.note.repository.NoteRepository;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.note.service.NoteServiceImpl;
import pl.michallysak.notes.user.service.CurrentUserProvider;
import pl.michallysak.notes.user.service.NoAuthCurrentUserProvider;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class NoteBeansTest {

    @Test
    void rootPresenter_shouldWireDependenciesAndBeSingleton() {
        // when
        NoteBeans noteBeans = new NoteBeans(new String[]{});
        Presenter rootPresenter1 = noteBeans.rootPresenter();
        Presenter rootPresenter2 = noteBeans.rootPresenter();
        // then
        assertSame(rootPresenter1, rootPresenter2);
        assertInstanceOf(RootPresenter.class, rootPresenter1);
    }

    @Test
    void notePresenter_shouldWireDependenciesAndBeSingleton() {
        // when
        NoteBeans noteBeans = new NoteBeans(new String[]{});
        CliNotePresenter cliNotePresenter1 = noteBeans.notePresenter();
        CliNotePresenter cliNotePresenter2 = noteBeans.notePresenter();
        // then
        assertSame(cliNotePresenter1, cliNotePresenter2);
        assertInstanceOf(CliNotePresenter.class, cliNotePresenter1);
    }

    @Test
    void noteService_shouldWireDependenciesAndBeSingleton() {
        // when
        NoteBeans noteBeans = new NoteBeans(new String[]{});
        NoteService noteService1 = noteBeans.noteService();
        NoteService noteService2 = noteBeans.noteService();
        // then
        assertSame(noteService1, noteService2);
        assertInstanceOf(NoteServiceImpl.class, noteService1);
    }

    @Test
    void noteRepository_shouldBeSingleton() {
        // when
        NoteBeans noteBeans = new NoteBeans(new String[]{});
        // when
        NoteRepository noteRepository1 = noteBeans.noteRepository();
        NoteRepository noteRepository2 = noteBeans.noteRepository();
        // then
        assertSame(noteRepository1, noteRepository2);
        assertInstanceOf(InMemoryNoteRepository.class, noteRepository1);
    }

    @Test
    void console_shouldReturnNonNullIO() {
        // when
        NoteBeans noteBeans = new NoteBeans(new String[]{});
        // then
        assertInstanceOf(ConsoleIO.class, noteBeans.console());
    }

    @Test
    void getCurrentUserProvider_shouldBeSingleton() {
        // given
        NoteBeans noteBeans = new NoteBeans(new String[]{});
        // when
        CurrentUserProvider currentUserProvider1 = noteBeans.currentUserProvider();
        CurrentUserProvider currentUserProvider2 = noteBeans.currentUserProvider();
        // then
        assertSame(currentUserProvider1, currentUserProvider2);
        assertInstanceOf(NoAuthCurrentUserProvider.class, currentUserProvider1);
    }

    @Test
    void noteRepository_shouldUseInMemory_whenPersistenceFlagProvided() {
        // given
        NoteBeans noteBeans = new NoteBeans(new String[]{"--persistence=in-memory"});
        // when
        NoteRepository noteRepository = noteBeans.noteRepository();
        // then
        assertInstanceOf(InMemoryNoteRepository.class, noteRepository);
    }

    @Test
    void noteRepository_shouldUseInMemory_whenNoPersistenceFlagProvided() {
        // given
        NoteBeans noteBeans = new NoteBeans(new String[]{});
        // when
        NoteRepository noteRepository = noteBeans.noteRepository();
        // then
        assertInstanceOf(InMemoryNoteRepository.class, noteRepository);
    }
}
