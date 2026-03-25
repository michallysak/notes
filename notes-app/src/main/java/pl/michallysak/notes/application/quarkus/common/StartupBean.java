package pl.michallysak.notes.application.quarkus.common;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.auth.model.Password;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.service.UserService;

@ApplicationScoped
@RequiredArgsConstructor
public class StartupBean {
    private final Logger logger;
    private final UserService userService;

    void onStart(@Observes StartupEvent ev) {
        Email email = Email.of("admin@test.pl");
        Password password = Password.of("Admin123!");
        UserValue user = userService.createUser(new EmailPasswordCreateUser(email, password));
        logger.info("Created default user: " + user);
        AuthToken login = userService.login(new EmailPasswordLogin(email, password));
        logger.info("Login Successful: " + login);

    }

}