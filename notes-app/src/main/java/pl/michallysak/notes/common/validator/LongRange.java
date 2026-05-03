package pl.michallysak.notes.common.validator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LongRange implements Range<Long> {
  private final Long min;
  private final Long max;

  public static LongRange of(Long min, Long max) {
    if (min.compareTo(max) > 0) {
      throw new IllegalArgumentException("Min cannot be greater than max");
    }
    return new LongRange(min, max);
  }

  @Override
  public boolean check(Long value) {
    return value.compareTo(getMin()) >= 0 && value.compareTo(getMax()) <= 0;
  }

  @Override
  public String toString() {
    return "[" + min + ", " + max + "]";
  }
}
