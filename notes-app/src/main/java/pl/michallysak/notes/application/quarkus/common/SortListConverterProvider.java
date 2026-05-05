package pl.michallysak.notes.application.quarkus.common;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import pl.michallysak.notes.note.model.FieldSort;
import pl.michallysak.notes.note.model.SortDirection;

@Provider
public class SortListConverterProvider implements ParamConverterProvider {

  @Override
  public <T> ParamConverter<T> getConverter(
      Class<T> rawType, Type genericType, Annotation[] annotations) {
    if (rawType.equals(SortList.class)) {
      return (ParamConverter<T>) new SortListConverter();
    }
    return null;
  }

  private static class SortListConverter implements ParamConverter<SortList> {

    @Override
    public SortList fromString(String value) {
      if (value == null || value.isBlank()) {
        return new SortList(List.of());
      }
      List<FieldSort> result =
          Arrays.stream(value.split(","))
              .map(
                  entry -> {
                    String[] parts = entry.split(":");
                    String field = parts[0];
                    SortDirection direction =
                        parts.length > 1
                            ? SortDirection.valueOf(parts[1].toUpperCase())
                            : SortDirection.ASC;
                    return new FieldSort(field, direction);
                  })
              .toList();
      return new SortList(result);
    }

    @Override
    public String toString(SortList value) {
      return value.values().stream()
          .map(fs -> fs.field() + ":" + fs.direction())
          .collect(Collectors.joining(","));
    }
  }
}
