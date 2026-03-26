package pl.michallysak.notes.auth.service;

public interface PasswordHasher {
  byte[] hash(char[] password, byte[] salt);

  byte[] generateSalt();
}
