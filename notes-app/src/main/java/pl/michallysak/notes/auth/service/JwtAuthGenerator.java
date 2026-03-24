package pl.michallysak.notes.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import pl.michallysak.notes.auth.model.AuthToken;
import pl.michallysak.notes.user.model.UserValue;

import java.security.Key;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class JwtAuthGenerator implements AuthTokenGenerator<UserValue, AuthToken> {

    private final Key key;
    private final long expirationMs;

    public JwtAuthGenerator() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        this.expirationMs = 3600000; // 1 hour
    }

    // FIXME use this constructor instead of default one
    public JwtAuthGenerator(String base64Secret, long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
        this.expirationMs = expirationMs;
    }

    @Override
    public AuthToken generateToken(UserValue user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        String token = Jwts.builder()
                .setSubject(user.id().toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("roles", Set.of("USER"))
                .signWith(key)
                .compact();
        return new AuthToken(token, OffsetDateTime.ofInstant(expiry.toInstant(), ZoneOffset.UTC));
    }
}

