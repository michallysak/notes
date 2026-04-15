package pl.michallysak.notes.application.quarkus.note.domain;

import lombok.Getter;

@Getter
public class SseConnectionException extends Exception {
  private final boolean connectionTermination;
  private final String streamKey;

  public SseConnectionException(String streamKey, String message, boolean connectionTermination) {
    this(streamKey, message, null, connectionTermination);
  }

  public SseConnectionException(
      String streamKey, String message, Throwable cause, boolean connectionTermination) {
    super("%s, streamKey: %s, cause: ".formatted(message, streamKey), cause);
    this.streamKey = streamKey;
    this.connectionTermination = connectionTermination;
  }
}
