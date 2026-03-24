package pl.michallysak.notes.auth.domain;

import lombok.Getter;
import pl.michallysak.notes.auth.model.PBKDF2HashedPassword;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public final class PBKDF2PasswordCredential extends PasswordCredential {
    private final PBKDF2HashedPassword hashedPassword;

    public PBKDF2PasswordCredential(PBKDF2HashedPassword hashedPassword) {
        super(UUID.randomUUID(), OffsetDateTime.now());
        this.hashedPassword = Objects.requireNonNull(hashedPassword);
    }

    @Override
    public PBKDF2HashedPassword getHashedPassword() {
        return hashedPassword;
    }
}

