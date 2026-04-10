package pl.michallysak.notes.user.service;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@IfBuildProfile("test")
@ApplicationScoped
public class TestCurrentUserProvider implements CurrentUserProvider {

  @Override
  public UUID getCurrentUserId() {
    return UUID.fromString("00000000-0000-0000-0000-000000000000");
  }
}
