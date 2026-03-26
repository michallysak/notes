package pl.michallysak.notes.auth.service;

public interface AuthTokenGenerator<Input, Output> {
  Output generateToken(Input input);
}
