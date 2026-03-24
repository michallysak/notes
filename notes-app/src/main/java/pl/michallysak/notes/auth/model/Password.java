package pl.michallysak.notes.auth.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public final class Password {
    private final String value;

    private Password(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        this.value = value;
    }

    public static Password of(String value) {
        return new Password(value);
    }

    @Override
    public String toString() {
        return "[password]";
    }
}

