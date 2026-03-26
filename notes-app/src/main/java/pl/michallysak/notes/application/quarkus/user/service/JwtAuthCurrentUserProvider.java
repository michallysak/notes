package pl.michallysak.notes.application.quarkus.user.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;
import pl.michallysak.notes.auth.exception.AuthException;
import pl.michallysak.notes.user.service.CurrentUserProvider;

@Default
@RequestScoped
@RequiredArgsConstructor
public class JwtAuthCurrentUserProvider implements CurrentUserProvider {

  private final JsonWebToken jwt;

  @Override
  public UUID getCurrentUserId() {
    try {
      return Optional.ofNullable(jwt)
          .map(JsonWebToken::getSubject)
          .map(UUID::fromString)
          .orElse(null);
    } catch (IllegalArgumentException e) {
      throw new AuthException("Not authenticated", e);
    }
  }
}
