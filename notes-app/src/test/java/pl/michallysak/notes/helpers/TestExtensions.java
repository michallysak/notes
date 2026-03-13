package pl.michallysak.notes.helpers;

import java.util.Arrays;
import java.util.stream.Stream;

public final class TestExtensions {

    @SafeVarargs
    public static <T> Stream<T> concat(Stream<T>... streams) {
        return Arrays.stream(streams).flatMap(s -> s);
    }

    public static Stream<String> textsWithLength(int length) {
        if (length <= 0) {
            return Stream.empty();
        }
        return Stream.of("*".repeat(length));
    }

}
