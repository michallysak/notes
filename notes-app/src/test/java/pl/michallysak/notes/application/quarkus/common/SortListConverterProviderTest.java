package pl.michallysak.notes.application.quarkus.common;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.ws.rs.ext.ParamConverter;
import java.util.List;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.note.model.FieldSort;
import pl.michallysak.notes.note.model.SortDirection;

class SortListConverterProviderTest {

  private final SortListConverterProvider provider = new SortListConverterProvider();

  @Test
  void getConverter_shouldReturnSortListConverter_whenRawTypeIsSortList() {
    // when
    ParamConverter<SortList> converter = provider.getConverter(SortList.class, null, null);
    // then
    assertNotNull(converter);
  }

  @Test
  void getConverter_shouldReturnNull_whenRawTypeIsNotSortList() {
    // when
    ParamConverter<String> converter = provider.getConverter(String.class, null, null);
    // then
    assertNull(converter);
  }

  @Test
  void sortListConverter_fromString_shouldReturnEmptySortList_whenValueIsNull() {
    // given
    ParamConverter<SortList> converter = provider.getConverter(SortList.class, null, null);
    // when
    SortList result = converter.fromString(null);
    // then
    assertEquals(new SortList(List.of()), result);
  }

  @Test
  void sortListConverter_fromString_shouldReturnEmptySortList_whenValueIsBlank() {
    // given
    ParamConverter<SortList> converter = provider.getConverter(SortList.class, null, null);
    // when
    SortList result = converter.fromString("   ");
    // then
    assertEquals(new SortList(List.of()), result);
  }

  @Test
  void sortListConverter_fromString_shouldParseSingleFieldAsc_whenNoDirection() {
    // given
    ParamConverter<SortList> converter = provider.getConverter(SortList.class, null, null);
    // when
    SortList result = converter.fromString("title");
    // then
    assertEquals(new SortList(List.of(new FieldSort("title", SortDirection.ASC))), result);
  }

  @Test
  void sortListConverter_fromString_shouldParseSingleFieldDesc() {
    // given
    ParamConverter<SortList> converter = provider.getConverter(SortList.class, null, null);
    // when
    SortList result = converter.fromString("title:DESC");
    // then
    assertEquals(new SortList(List.of(new FieldSort("title", SortDirection.DESC))), result);
  }

  @Test
  void sortListConverter_fromString_shouldParseMultipleFields() {
    // given
    ParamConverter<SortList> converter = provider.getConverter(SortList.class, null, null);
    // when
    SortList result = converter.fromString("title:ASC,created:DESC");
    // then
    assertEquals(
        new SortList(
            List.of(
                new FieldSort("title", SortDirection.ASC),
                new FieldSort("created", SortDirection.DESC))),
        result);
  }

  @Test
  void sortListConverter_toString_shouldConvertSortListToString() {
    // given
    ParamConverter<SortList> converter = provider.getConverter(SortList.class, null, null);
    SortList sortList =
        new SortList(
            List.of(
                new FieldSort("title", SortDirection.ASC),
                new FieldSort("created", SortDirection.DESC)));
    // when
    String result = converter.toString(sortList);
    // then
    assertEquals("title:ASC,created:DESC", result);
  }

  @Test
  void sortListConverter_toString_shouldConvertEmptySortListToEmptyString() {
    // given
    ParamConverter<SortList> converter = provider.getConverter(SortList.class, null, null);
    SortList sortList = new SortList(List.of());
    // when
    String result = converter.toString(sortList);
    // then
    assertEquals("", result);
  }
}
