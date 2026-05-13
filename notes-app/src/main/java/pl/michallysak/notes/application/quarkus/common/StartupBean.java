package pl.michallysak.notes.application.quarkus.common;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.auth.model.Password;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NotePermission;
import pl.michallysak.notes.note.model.NoteStyle;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.model.SetNotePermissions;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.repository.UserRepository;
import pl.michallysak.notes.user.service.UserService;

@ApplicationScoped
@RequiredArgsConstructor
public class StartupBean {
  private static final int STARTUP_NOTES_COUNT = 30;
  private static final int SHARED_PER_GROUP_COUNT = 5;

  private final Logger logger;
  private final UserRepository userRepository;
  private final UserService userService;
  private final NoteService noteService;

  void onStart(@Observes StartupEvent ev) {
    Email email = Email.of("admin@test.pl");
    Password password = Password.of("Admin123!");
    UserValue user = getUserValue(email, password);
    UserValue sharedUser = getUserValue(Email.of("user@test.pl"), Password.of("User123!"));

    AuthToken login = userService.login(new EmailPasswordLogin(email, password));
    logger.info("Login Successful: " + login);

    if (!noteService.getCreatedNotes(user.id()).isEmpty()) {
      return;
    }

    createStartupNotes(user, sharedUser);
  }

  private void createStartupNotes(UserValue owner, UserValue sharedUser) {
    int sharedPinnedCount = 0;
    int sharedUnpinnedCount = 0;

    for (int i = 1; i <= STARTUP_NOTES_COUNT; i++) {
      boolean isPinned = i <= STARTUP_NOTES_COUNT / 2;
      NoteValue created = noteService.createNote(getCreateNote(owner, String.valueOf(i)));
      logger.info("Created note: " + created);

      if (isPinned) {
        NoteUpdate noteUpdate =
            NoteUpdate.builder()
                .pinned(true)
                .actingUserId(owner.id())
                .style(NoteStyle.builder().color("#b03a3a").build())
                .build();
        NoteValue updated = noteService.updateNote(created.id(), noteUpdate);
        logger.info("Pinned note: " + updated);
      }

      if (isPinned && sharedPinnedCount < SHARED_PER_GROUP_COUNT) {
        shareNoteWithUser(created.id(), owner.id(), sharedUser.id());
        sharedPinnedCount++;
      } else if (!isPinned && sharedUnpinnedCount < SHARED_PER_GROUP_COUNT) {
        shareNoteWithUser(created.id(), owner.id(), sharedUser.id());
        sharedUnpinnedCount++;
      }
    }
  }

  private void shareNoteWithUser(UUID noteId, UUID actingUserId, UUID targetUserId) {
    SetNotePermissions permissions =
        new SetNotePermissions(targetUserId, Set.of(NotePermission.READ));
    noteService.setPermissions(noteId, actingUserId, permissions);
    logger.info("Shared note %s with %s".formatted(noteId, targetUserId));
  }

  private UserValue getUserValue(Email email, Password password) {
    return userRepository
        .findUserWithEmail(email)
        .map(UserValue::from)
        .orElseGet(
            () -> {
              EmailPasswordCreateUser createUser = new EmailPasswordCreateUser(email, password);
              UserValue user = userService.createUser(createUser);
              logger.info("Created default user: " + user);
              return user;
            });
  }

  private CreateNote getCreateNote(UserValue user, String distinguishingText) {
    String title = "Note %s".formatted(distinguishingText);
    String content = "This is the content of the %s note".formatted(distinguishingText);
    return new CreateNote(title, content, user.id());
  }
}
