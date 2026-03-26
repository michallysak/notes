package pl.michallysak.notes.application.cli.io;

public interface Input<T> {
  T readLine(String prompt);
}
