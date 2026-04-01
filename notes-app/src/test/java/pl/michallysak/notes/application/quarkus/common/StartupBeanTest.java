package pl.michallysak.notes.application.quarkus.common;

import static org.mockito.Mockito.*;

import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.auth.model.Password;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.service.UserService;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class StartupBeanTest {
  @Mock Logger logger;
  @Mock UserService userService;
  @Mock NoteService noteService;
  @InjectMocks StartupBean startupBean;

  @Test
  void onStart_shouldCreateAndLoginUser_andLogInfo() {
    // given
    Email email = Email.of("admin@test.pl");
    Password password = Password.of("Admin123!");
    UserValue user = mock(UserValue.class);
    when(user.id()).thenReturn(UUID.randomUUID());
    when(userService.createUser(any(EmailPasswordCreateUser.class))).thenReturn(user);
    when(userService.login(any(EmailPasswordLogin.class))).thenReturn(mock(AuthToken.class));
    // and
    NoteValue firstNote = mock(NoteValue.class);
    NoteValue secondNote = mock(NoteValue.class);
    NoteValue updatedNote = mock(NoteValue.class);
    UUID secondNoteId = UUID.randomUUID();
    when(secondNote.id()).thenReturn(secondNoteId);
    when(noteService.createNote(argThat(note -> note != null && note.title().contains("first") && note.content().contains("first") && note.authorId().equals(user.id())))).thenReturn(firstNote);
    when(noteService.createNote(argThat(note -> note != null && note.title().contains("second") && note.content().contains("second") && note.authorId().equals(user.id())))).thenReturn(secondNote);
    when(noteService.updateNote(any(), any())).thenReturn(updatedNote);

    // when
    startupBean.onStart(mock(StartupEvent.class));

    // then
    verify(userService)
        .createUser(argThat(arg -> arg.email().equals(email) && arg.password().equals(password)));
    verify(userService)
        .login(argThat(arg -> arg.email().equals(email) && arg.password().equals(password)));
    verify(noteService).createNote(argThat(note -> note.title().equals("Note first") && note.content().contains("first") && note.authorId().equals(user.id())));
    verify(noteService).createNote(argThat(note -> note.title().equals("Note second") && note.content().contains("second") && note.authorId().equals(user.id())));
    verify(noteService).updateNote(eq(secondNoteId), argThat(update -> Boolean.TRUE.equals(update.pinned())));
    verify(logger).info(contains("Created default user:"));
    verify(logger).info(contains("Login Successful:"));
    verify(logger).info(contains("Created first note:"));
    verify(logger).info(contains("Created second note:"));
    verify(logger).info(contains("Updated second note:"));
  }
}
