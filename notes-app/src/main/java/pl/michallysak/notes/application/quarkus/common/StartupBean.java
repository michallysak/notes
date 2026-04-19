package pl.michallysak.notes.application.quarkus.common;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.auth.model.Password;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.repository.UserRepository;
import pl.michallysak.notes.user.service.UserService;

@ApplicationScoped
@RequiredArgsConstructor
public class StartupBean {
  private final Logger logger;
  private final UserRepository userRepository;
  private final UserService userService;
  private final NoteService noteService;

  void onStart(@Observes StartupEvent ev) {
    Email email = Email.of("admin@test.pl");
    Password password = Password.of("Admin123!");
    UserValue user = getUserValue(email, password);

    AuthToken login = userService.login(new EmailPasswordLogin(email, password));
    logger.info("Login Successful: " + login);

    if (!noteService.getCreatedNotes(user.id()).isEmpty()) {
      return;
    }

    NoteValue first = noteService.createNote(getCreateNote(user, "first"));
    logger.info("Created first note: " + first);
    NoteValue second = noteService.createNote(getCreateNote(user, "second"));
    logger.info("Created second note: " + second);
    NoteUpdate noteUpdate = NoteUpdate.builder().pinned(true).actingUserId(user.id()).build();
    NoteValue noteValue = noteService.updateNote(second.id(), noteUpdate);
    logger.info("Updated second note: " + noteValue);
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
