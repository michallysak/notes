package pl.michallysak.notes.application.quarkus.common;

import static org.mockito.Mockito.*;

import io.quarkus.runtime.StartupEvent;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.auth.model.Password;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NotePermission;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.repository.UserRepository;
import pl.michallysak.notes.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class StartupBeanTest {
  @Mock Logger logger;
  @Mock UserRepository userRepository;
  @Mock UserService userService;
  @Mock NoteService noteService;
  @InjectMocks StartupBean startupBean;

  @Test
  void onStart_shouldCreateNotesPinHalfAndShareFivePerGroup() {
    // given
    Email adminEmail = Email.of("admin@test.pl");
    Password adminPassword = Password.of("Admin123!");
    UUID adminUserId = UUID.randomUUID();
    UUID sharedUserId = UUID.randomUUID();

    UserValue adminUser = mock(UserValue.class);
    when(adminUser.id()).thenReturn(adminUserId);
    UserValue sharedUser = mock(UserValue.class);
    when(sharedUser.id()).thenReturn(sharedUserId);

    when(userRepository.findUserWithEmail(any(Email.class))).thenReturn(Optional.empty());
    when(userService.createUser(any(EmailPasswordCreateUser.class)))
        .thenReturn(adminUser, sharedUser);
    when(userService.login(any(EmailPasswordLogin.class))).thenReturn(mock(AuthToken.class));
    when(noteService.getCreatedNotes(eq(adminUserId))).thenReturn(List.of());

    when(noteService.createNote(any(CreateNote.class)))
        .thenAnswer(
            invocation -> {
              UUID noteId = UUID.randomUUID();
              return NoteValue.builder().id(noteId).build();
            });
    when(noteService.updateNote(any(UUID.class), any(NoteUpdate.class)))
        .thenAnswer(invocation -> NoteValue.builder().id(invocation.getArgument(0)).build());

    // when
    startupBean.onStart(mock(StartupEvent.class));

    // then
    verify(userService)
        .createUser(
            argThat(arg -> arg.email().equals(adminEmail) && arg.password().equals(adminPassword)));
    verify(userService)
        .createUser(
            argThat(
                arg ->
                    arg.email().equals(Email.of("user@test.pl"))
                        && arg.password().equals(Password.of("User123!"))));
    verify(noteService).getCreatedNotes(eq(adminUserId));
    verify(userService)
        .login(
            argThat(arg -> arg.email().equals(adminEmail) && arg.password().equals(adminPassword)));

    verify(noteService, times(30))
        .createNote(argThat(note -> note != null && note.authorId().equals(adminUserId)));
    verify(noteService, times(15))
        .updateNote(
            any(UUID.class),
            argThat(
                update ->
                    Boolean.TRUE.equals(update.pinned())
                        && update.actingUserId().equals(adminUserId)));
    verify(noteService, times(10))
        .setPermissions(
            any(UUID.class),
            eq(adminUserId),
            argThat(
                request ->
                    request.targetUserId().equals(sharedUserId)
                        && request.permissions().equals(Set.of(NotePermission.READ))));

    verify(logger, times(2)).info(contains("Created default user:"));
    verify(logger).info(contains("Login Successful:"));
    verify(logger, atLeast(30)).info(contains("Created note:"));
    verify(logger, atLeast(15)).info(contains("Pinned note:"));
    verify(logger, atLeast(10)).info(contains("Shared note"));
  }

  @Test
  void onStart_shouldSkipNoteCreation_whenNotesAlreadyExist() {
    // given
    UUID adminUserId = UUID.randomUUID();
    UserValue adminUser = mock(UserValue.class);
    when(adminUser.id()).thenReturn(adminUserId);
    UserValue sharedUser = mock(UserValue.class);

    when(userRepository.findUserWithEmail(any(Email.class))).thenReturn(Optional.empty());
    when(userService.createUser(any(EmailPasswordCreateUser.class)))
        .thenReturn(adminUser, sharedUser);
    when(userService.login(any(EmailPasswordLogin.class))).thenReturn(mock(AuthToken.class));
    when(noteService.getCreatedNotes(eq(adminUserId))).thenReturn(List.of(mock(NoteValue.class)));

    // when
    startupBean.onStart(mock(StartupEvent.class));

    // then
    verify(noteService, never()).createNote(any());
    verify(noteService, never()).updateNote(any(), any());
    verify(noteService, never()).setPermissions(any(), any(), any());
  }
}
