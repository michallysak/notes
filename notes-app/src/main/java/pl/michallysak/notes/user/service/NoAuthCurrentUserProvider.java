package pl.michallysak.notes.user.service;

import lombok.Getter;
import java.util.UUID;

@Getter
public class NoAuthCurrentUserProvider implements CurrentUserProvider {
	private final UUID currentUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
	@Override
	public UUID getCurrentUserId() {
		return currentUserId;
	}
}

