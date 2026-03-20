package pl.michallysak.notes.user.domain;

import pl.michallysak.notes.common.Email;

import java.util.UUID;

public interface User {
    UUID getId();
    Email getEmail();
}

