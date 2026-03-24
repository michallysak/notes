package pl.michallysak.notes.user.model;

import lombok.Builder;
import pl.michallysak.notes.auth.model.Password;
import pl.michallysak.notes.common.Email;

@Builder
public record EmailPasswordLogin(Email email, Password password) {

}
