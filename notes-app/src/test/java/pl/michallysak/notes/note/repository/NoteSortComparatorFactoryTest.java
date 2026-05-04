package pl.michallysak.notes.note.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.model.FieldSort;
import pl.michallysak.notes.note.model.SortDirection;

class NoteSortComparatorFactoryTest {

  private final NoteSortComparatorFactory factory = new NoteSortComparatorFactory();

  @Test
  void createComparator_shouldReturnDefault_whenNullSort() {
    // given
    List<FieldSort> fieldSorts = null;
    // when
    Comparator<Note> comparator = factory.createComparator(fieldSorts);
    // then
    assertNotNull(comparator);
  }

  @Test
  void createComparator_shouldReturnDefault_whenEmptySort() {
    // given
    List<FieldSort> fieldSorts = List.of();
    // when
    Comparator<Note> comparator = factory.createComparator(fieldSorts);
    // then
    assertNotNull(comparator);
  }

  @Test
  void createComparator_shouldBuildComparator_whenValidSort() {
    // given
    List<FieldSort> sorts = List.of(new FieldSort("title", SortDirection.ASC));
    // when
    Comparator<Note> comparator = factory.createComparator(sorts);
    // then
    assertNotNull(comparator);
  }

  @Test
  void createComparator_shouldReturnCreatedComparator_whenNullField() {
    // given
    String field = null;
    // when
    Comparator<Note> comparator = factory.createComparator(field);
    // then
    assertNotNull(comparator);
  }

  @Test
  void createComparator_shouldReturnCreatedComparator_whenInvalidField() {
    // given
    String invalid = "invalid";
    // when
    Comparator<Note> comparator = factory.createComparator(invalid);
    // then
    assertNotNull(comparator);
  }

  @ParameterizedTest
  @ValueSource(strings = {"updated", "title", "pinned"})
  void createComparator_shouldReturnComparator_whenValidField(String field) {
    // when
    Comparator<Note> comparator = factory.createComparator(field);
    // then
    assertNotNull(comparator);
  }

  @Test
  void createComparator_shouldCompareUpdatedFieldCorrectly() {
    // given
    String field = "updated";
    Note note1 = createNoteWithUpdated(OffsetDateTime.now().minusDays(1));
    Note note2 = createNoteWithUpdated(OffsetDateTime.now());
    // when
    Comparator<Note> comparator = factory.createComparator(field);
    int result = comparator.compare(note1, note2);
    // then
    assertTrue(result < 0);
  }

  private Note createNoteWithUpdated(OffsetDateTime updated) {
    Note note = mock(Note.class);
    when(note.getUpdated()).thenReturn(java.util.Optional.of(updated));
    return note;
  }
}
