package pl.michallysak.notes.application.cli.presenter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.cli.note.beans.NoteBeans;
import pl.michallysak.notes.application.cli.io.IO;
import pl.michallysak.notes.application.cli.note.presenter.CliNotePresenter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RootPresenterTest {
    @Mock
    IO<String> io;
    @Mock
    NoteBeans noteBeans;
    @Mock
    CliNotePresenter cliNotePresenter;
    @InjectMocks
    RootPresenter rootPresenter;

    @BeforeEach
    void setUp() {
        when(noteBeans.console()).thenReturn(io);
        when(noteBeans.notePresenter()).thenReturn(cliNotePresenter);
    }

    @Test
    void present_shouldDelegateToCliNotePresenter() {
        rootPresenter.present();
        verify(io).println("Starting CLI Notes Application...");
        verify(noteBeans).notePresenter();
        verify(cliNotePresenter).present();
        verify(io).println("Exiting CLI Notes Application...");
    }
}
