package pl.michallysak.notes.application.cli.note.presenter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoteOption {
    CREATE("1"),
    LIST("2"),
    SHOW("3"),
    UPDATE("4"),
    DELETE("5"),
    EXIT("0");

    private final String value;

    public static NoteOption fromValue(String value) {
        for (NoteOption option : values()) {
            if (option.value.equals(value)) {
                return option;
            }
        }
        return null;
    }
}

