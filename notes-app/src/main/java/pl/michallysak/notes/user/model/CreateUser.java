package pl.michallysak.notes.user.model;

import lombok.Builder;
import pl.michallysak.notes.common.Email;

@Builder
public record CreateUser(Email email, String password) {}

