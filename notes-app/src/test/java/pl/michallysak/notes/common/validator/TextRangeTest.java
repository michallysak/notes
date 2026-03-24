package pl.michallysak.notes.common.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TextRangeTest {

    @Test
    void of_shouldCreateTextRange_whenValidMinMax() {
        // when
        TextRange range = TextRange.of(1, 5);
        // then
        assertNotNull(range);
        assertTrue(range.check(3));
    }

    @Test
    void of_shouldThrowException_whenMinGreaterThanMax() {
        // when
        Executable executable = () -> TextRange.of(5, 1);
        // then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Min cannot be greater than max", exception.getMessage());
    }

    @Test
    void of_shouldThrowException_whenNegativeMin() {
        // when
        Executable executable = () -> TextRange.of(-1, 1);
        // then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Min cannot be negative", exception.getMessage());
    }

    @Test
    void check_shouldThrowException_whenNegativeValue() {
        // given
        TextRange range = TextRange.of(1, 5);
        // then
        Executable executable = () -> range.check(-1);
        // then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Value cannot be negative", exception.getMessage());
    }

    @ParameterizedTest()
    @ValueSource(ints = {1, 3, 5})
    void check_shouldReturnTrue_whenValueInRange(int value) {
        // when
        TextRange range = TextRange.of(1, 5);
        // then
        boolean result = range.check(value);
        // then
        assertTrue(result);
    }

    @ParameterizedTest()
    @ValueSource(ints = {0, 6})
    void check_shouldReturnFalse_whenValueOutOfRange(int value) {
        // when
        TextRange range = TextRange.of(1, 5);
        // then
        boolean result = range.check(value);
        // then
        assertFalse(result);
    }

    @Test
    void toString_shouldReturnCorrectFormat() {
        // when
        TextRange range = TextRange.of(1, 5);
        // then
        assertEquals("[1, 5]", range.toString());
    }

    @Test
    void getMin_shouldReturnMin() {
        // when
        TextRange range = TextRange.of(2, 7);
        // then
        assertEquals(2, range.getMin());
    }

    @Test
    void getMax_shouldReturnMax() {
        // when
        TextRange range = TextRange.of(2, 7);
        // then
        assertEquals(7, range.getMax());
    }

}
