package pl.michallysak.notes.application.quarkus.user.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.auth.service.AuthTokenGenerator;
import pl.michallysak.notes.user.model.UserValue;

@Default
@ApplicationScoped
public class QuarkusJwtAuthGenerator implements AuthTokenGenerator<UserValue, AuthToken> {
  private static final Duration DEFAULT_EXPIRY = Duration.ofHours(1);

  @Override
  public AuthToken generateToken(UserValue user) {
    Instant now = Instant.now();
    Instant exp = now.plus(DEFAULT_EXPIRY);
    String token =
        Jwt.issuer("notes-app").subject(user.id().toString()).issuedAt(now).expiresAt(exp).sign();
    return new AuthToken(token, exp.atOffset(OffsetDateTime.now().getOffset()));
  }
}
