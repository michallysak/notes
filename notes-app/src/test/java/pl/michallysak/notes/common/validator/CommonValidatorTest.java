package pl.michallysak.notes.common.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import pl.michallysak.notes.common.exception.ValidationException;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class CommonValidatorTest {

    private final CommonValidator commonValidator = new CommonValidator();

    @Test
    void throwOnNull_shouldThrow_whenNull() {
        // given
        String text = null;
        String message = "Text cannot be null";
        // when
        Executable executable = () -> commonValidator.throwOnNull(text, message);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        assertEquals(message, validationException.getMessage());
    }

    @ParameterizedTest
    @EmptySource
    void throwOnNull_shouldNotThrow_whenNonNull(String text) {
        // given
        String message = "Text cannot be null";
        // when
        Executable executable = () -> commonValidator.throwOnNull(text, message);
        // then
        assertDoesNotThrow(executable);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void throwOnNotInRange_shouldThrow_whenLengthInRange(int length) {
        // given
        TextRange range = TextRange.of(1, 3);
        String text = "X".repeat(length);
        String message = "Text cannot be null";
        // when
        Executable executable = () -> commonValidator.throwOnNotInRange(text, range, message);
        // then
        assertDoesNotThrow(executable);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4})
    void throwOnNotInRange_shouldNotThrow_whenLengthNotInRange(int length) {
        // given
        String text = "X".repeat(length);
        TextRange range = TextRange.of(1, 3);
        String message = "Text cannot be null";
        // when
        Executable executable = () -> commonValidator.throwOnNotInRange(text, range, message);
        // then
        ValidationException validationException = assertThrows(ValidationException.class, executable);
        assertEquals(message, validationException.getMessage());
    }

    @Test
    void throwOnNotMatch_shouldThrow_whenPatternDoesNotMatch() {
        // given
        String text = "abc";
        Pattern pattern = Pattern.compile("\\d+");
        String message = "Text must be digits";
        // when
        Executable executable = () -> commonValidator.throwOnNotMatch(text, pattern, message);
        // then
        ValidationException exception = assertThrows(ValidationException.class, executable);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void throwOnNotMatch_shouldNotThrow_whenPatternMatches() {
        // given
        String text = "12345";
        Pattern pattern = Pattern.compile("\\d+");
        String message = "Text must be digits";
        // when
        Executable executable = () -> commonValidator.throwOnNotMatch(text, pattern, message);
        // then
        assertDoesNotThrow(executable);
    }

}
