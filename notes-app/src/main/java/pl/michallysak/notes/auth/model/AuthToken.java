package pl.michallysak.notes.auth.model;

import java.time.OffsetDateTime;

public record AuthToken(String token, OffsetDateTime expiresAt) {}
