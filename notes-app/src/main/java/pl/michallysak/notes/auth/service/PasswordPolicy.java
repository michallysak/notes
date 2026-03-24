package pl.michallysak.notes.auth.service;

import pl.michallysak.notes.auth.domain.PasswordCredential;
import pl.michallysak.notes.auth.model.Password;

public interface PasswordPolicy {

    boolean isUpToDate(PasswordCredential credential);

    PasswordCredential hash(Password password);

    boolean verifyPassword(Password password, PasswordCredential credential);
}
