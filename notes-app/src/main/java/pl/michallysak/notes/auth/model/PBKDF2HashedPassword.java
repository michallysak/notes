package pl.michallysak.notes.auth.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public final class PBKDF2HashedPassword extends HashedPassword {
    private final int iterations;

    public PBKDF2HashedPassword(byte[] hash, byte[] salt, int iterations) {
        super(hash, salt);
        this.iterations = iterations;
    }

}
