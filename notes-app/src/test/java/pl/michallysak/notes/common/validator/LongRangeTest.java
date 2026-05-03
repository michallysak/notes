package pl.michallysak.notes.common.validator;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LongRangeTest {

  @Test
  void of_shouldCreateRange_whenMinLessThanOrEqualMax() {
    // given
    Long min = 1L;
    Long max = 5L;
    // when
    LongRange range = LongRange.of(min, max);
    // then
    assertEquals(min, range.getMin());
    assertEquals(max, range.getMax());
  }

  @Test
  void of_shouldThrow_whenMinGreaterThanMax() {
    // given
    Long min = 5L;
    Long max = 1L;
    // when
    Executable executable = () -> LongRange.of(min, max);
    // then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
    assertEquals("Min cannot be greater than max", exception.getMessage());
  }

  @ParameterizedTest
  @ValueSource(longs = {1L, 2L, 3L, 4L, 5L})
  void check_shouldReturnTrue_whenValueInRange(long value) {
    // given
    LongRange range = LongRange.of(1L, 5L);
    // when
    boolean result = range.check(value);
    // then
    assertTrue(result);
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, 6L, -1L})
  void check_shouldReturnFalse_whenValueNotInRange(long value) {
    // given
    LongRange range = LongRange.of(1L, 5L);
    // when
    boolean result = range.check(value);
    // then
    assertFalse(result);
  }

  @Test
  void toString_shouldReturnFormattedString() {
    // given
    LongRange range = LongRange.of(1L, 5L);
    // when
    String result = range.toString();
    // then
    assertEquals("[1, 5]", result);
  }
}
