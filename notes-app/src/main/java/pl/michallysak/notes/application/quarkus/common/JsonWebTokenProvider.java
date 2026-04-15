package pl.michallysak.notes.application.quarkus.common;

import jakarta.enterprise.context.RequestScoped;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

@RequestScoped
@RequiredArgsConstructor
public class JsonWebTokenProvider {
  private final JsonWebToken jwt;

  public Optional<Instant> getExpired() {
    return Optional.ofNullable(jwt)
        .map(j -> j.getClaim("exp"))
        .filter(Number.class::isInstance)
        .map(Number.class::cast)
        .map(n -> Instant.ofEpochSecond(n.longValue()))
        .filter(i -> i.isAfter(Instant.now()));
  }
}
