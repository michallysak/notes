package pl.michallysak.notes.common.validator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TextRange implements Range<Integer> {
  private final Integer min;
  private final Integer max;

  public static TextRange of(Integer min, Integer max) {
    if (min.compareTo(max) > 0) {
      throw new IllegalArgumentException("Min cannot be greater than max");
    }
    if (min < 0) {
      throw new IllegalArgumentException("Min cannot be negative");
    }
    return new TextRange(min, max);
  }

  @Override
  public boolean check(Integer value) {
    if (value < 0) {
      throw new IllegalArgumentException("Value cannot be negative");
    }
    return value.compareTo(getMin()) >= 0 && value.compareTo(getMax()) <= 0;
  }

  @Override
  public String toString() {
    return "[" + min + ", " + max + "]";
  }
}
