package pl.michallysak.notes.common.validator;

public interface Range<T extends Number & Comparable<T>> {
  boolean check(T value);

  T getMin();

  T getMax();
}
