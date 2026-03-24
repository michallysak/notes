package pl.michallysak.notes.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.auth.model.HashedPassword;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public abstract class PasswordCredential implements Credential {
    protected final UUID id;
    protected final OffsetDateTime createdAt;

    public abstract HashedPassword getHashedPassword();
}
