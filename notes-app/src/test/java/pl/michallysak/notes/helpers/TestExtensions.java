package pl.michallysak.notes.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.SneakyThrows;

public final class TestExtensions {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @SafeVarargs
  public static <T> Stream<T> concat(Stream<T>... streams) {
    return Arrays.stream(streams).flatMap(s -> s);
  }

  public static Stream<String> textsWithLength(int length) {
    return textsWithLength(length, '*');
  }

  public static Stream<String> textsWithLength(int length, char repeated) {
    if (length <= 0) {
      return Stream.empty();
    }
    String repeatedString = String.valueOf(repeated);
    return Stream.of(repeatedString.repeat(length));
  }

  @SneakyThrows
  public static String toJsonString(Object object) {
    return objectMapper.writeValueAsString(object);
  }
}
