package pl.michallysak.notes.application.quarkus.user.domain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.auth.model.Password;
import pl.michallysak.notes.auth.service.AuthTokenGenerator;
import pl.michallysak.notes.auth.service.PBKDF2Hasher;
import pl.michallysak.notes.auth.service.PasswordPolicy;
import pl.michallysak.notes.auth.service.PasswordPolicyImpl;
import pl.michallysak.notes.auth.validator.PasswordStrengthValidator;
import pl.michallysak.notes.auth.validator.PasswordStrengthValidatorImpl;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.model.EmailPasswordCreateUser;
import pl.michallysak.notes.user.model.EmailPasswordLogin;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.repository.InMemoryUserRepository;
import pl.michallysak.notes.user.repository.UserRepository;
import pl.michallysak.notes.user.service.*;
import pl.michallysak.notes.user.validator.UserValidator;
import pl.michallysak.notes.user.validator.UserValidatorImpl;

@ApplicationScoped
@RequiredArgsConstructor
public class UserBeans {
    private final Logger logger;

    @Produces
    @NoAuth
    @ApplicationScoped
    public CurrentUserProvider currentUserProvider() {
        return new NoAuthCurrentUserProvider();
    }

    @Produces
    @ApplicationScoped
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    @Produces
    @ApplicationScoped
    public PasswordStrengthValidator passwordStrengthValidator() {
        return new PasswordStrengthValidatorImpl();
    }

    @Produces
    @ApplicationScoped
    public UserValidator userValidator(PasswordStrengthValidator passwordStrengthValidator) {
        return new UserValidatorImpl(passwordStrengthValidator);
    }

    @Produces
    @ApplicationScoped
    public PBKDF2Hasher pbkdf2Hasher() {
        return new PBKDF2Hasher(5000);
    }

    @Produces
    @ApplicationScoped
    public PasswordPolicy passwordPolicy(PBKDF2Hasher pbkdf2Hasher) {
        return new PasswordPolicyImpl(pbkdf2Hasher, logger);
    }

    @Produces
    @ApplicationScoped
    public UserService userService(Logger logger, UserRepository userRepository, UserValidator userValidator, AuthTokenGenerator<UserValue, AuthToken> tokenGenerator, PasswordPolicy passwordPolicy) {
        return new UserServiceImpl(logger, userRepository, userValidator, tokenGenerator, passwordPolicy);
    }
}

