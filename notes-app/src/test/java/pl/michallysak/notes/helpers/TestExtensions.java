package pl.michallysak.notes.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.stream.Stream;

public final class TestExtensions {

    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    @SneakyThrows
    public static String toJsonString(Object object) {
        return objectMapper.writeValueAsString(object);
    }
}
