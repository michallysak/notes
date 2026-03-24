package pl.michallysak.notes.auth.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

@Getter
@EqualsAndHashCode
public class HashedPassword {
    private final byte[] hash;
    private final byte[] salt;

    public HashedPassword(byte[] hash, byte[] salt) {
        this.hash = Objects.requireNonNull(hash);
        this.salt = Objects.requireNonNull(salt);
    }

}
