package pl.michallysak.notes.user.service;

import java.util.UUID;

public interface CurrentUserProvider {
    UUID getCurrentUserId();
}
