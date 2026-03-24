package pl.michallysak.notes.auth.domain;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class TestCredential implements Credential {
    private final OffsetDateTime createdAt;
    private final UUID id;

    public TestCredential() {
        this.id = UUID.randomUUID();
        this.createdAt = OffsetDateTime.now();
    }
}
