package pl.michallysak.notes.user.model;

import pl.michallysak.notes.common.Email;

public record RegisterUser(Email email, String password) {}
