package pl.michallysak.notes.application.cli.io;

public interface Output<T> {
    void print(T message);
    void println(T message);
}

