package pl.michallysak.notes.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import pl.michallysak.notes.note.model.FieldSort;
import pl.michallysak.notes.note.model.SortDirection;

class SortComparatorFactoryTest {

  private final SortComparatorFactory<TestEntity> comparator = new TestEntityComparatorFactory();

  @Test
  void shouldCreateComparatorWithSingleFieldAsc() {
    // given
    List<FieldSort> fieldSorts = List.of(new FieldSort("name", SortDirection.ASC));
    // when
    Comparator<TestEntity> result = comparator.createComparator(fieldSorts);
    // then
    TestEntity a = new TestEntity("A");
    TestEntity b = new TestEntity("B");
    assertTrue(result.compare(a, b) < 0);
    assertTrue(result.compare(b, a) > 0);
  }

  @Test
  void shouldCreateComparatorWithSingleFieldDesc() {
    // given
    List<FieldSort> fieldSorts = List.of(new FieldSort("name", SortDirection.DESC));
    // when
    Comparator<TestEntity> result = comparator.createComparator(fieldSorts);
    // then
    TestEntity a = new TestEntity("A");
    TestEntity b = new TestEntity("B");
    assertTrue(result.compare(a, b) > 0);
    assertTrue(result.compare(b, a) < 0);
  }

  @Test
  void shouldCreateComparatorWithMultipleFields() {
    // given
    List<FieldSort> fieldSorts =
        List.of(
            new FieldSort("name", SortDirection.ASC), new FieldSort("value", SortDirection.DESC));
    // when
    Comparator<TestEntity> result = comparator.createComparator(fieldSorts);
    // then
    TestEntity a1 = new TestEntity("A", 1);
    TestEntity a2 = new TestEntity("A", 2);
    assertTrue(result.compare(a1, a2) > 0); // same name, higher value first (DESC)
    TestEntity b = new TestEntity("B", 1);
    assertTrue(result.compare(a1, b) < 0); // A before B
  }

  private static class TestEntityComparatorFactory implements SortComparatorFactory<TestEntity> {
    @Override
    public Comparator<TestEntity> createComparator(String field) {
      return switch (field) {
        case "name" -> Comparator.comparing(TestEntity::name);
        case "value" -> Comparator.comparing(TestEntity::value);
        default -> Comparator.comparing(TestEntity::name);
      };
    }
  }

  record TestEntity(String name, int value) {
    TestEntity(String name) {
      this(name, 0);
    }
  }
}
