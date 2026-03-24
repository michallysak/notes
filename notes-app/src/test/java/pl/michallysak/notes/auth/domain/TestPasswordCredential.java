package pl.michallysak.notes.auth.domain;

import pl.michallysak.notes.auth.model.HashedPassword;
import java.util.UUID;
import java.time.OffsetDateTime;

public class TestPasswordCredential extends PasswordCredential {
    private final byte[] salt;
    private final byte[] hash;

    public TestPasswordCredential() {
        super(UUID.randomUUID(), OffsetDateTime.now());
        this.hash = new byte[]{1, 2, 3};
        this.salt = new byte[]{1, 2, 3};
    }

    public TestPasswordCredential(byte[] hash, byte[] salt) {
        super(UUID.randomUUID(), OffsetDateTime.now());
        this.hash = hash;
        this.salt = salt;
    }

    @Override
    public HashedPassword getHashedPassword() {
        return new HashedPassword(hash, salt);
    }
}

