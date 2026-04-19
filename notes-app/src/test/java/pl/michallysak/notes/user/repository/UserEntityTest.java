package pl.michallysak.notes.user.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserEntityTest {

  @Test
  void setCredentials_shouldHandleNullCredentials() {
    // given
    UserEntity userEntity = getUserEntity();
    userEntity.addCredential(new UserCredentialEntity());
    // when
    userEntity.setCredentials(null);
    // then
    assertTrue(userEntity.getCredentials().isEmpty());
  }

  @Test
  void addCredential_shouldHandleNullCredential() {
    // given
    UserEntity userEntity = getUserEntity();
    // when
    userEntity.addCredential(null);
    // then
    assertTrue(userEntity.getCredentials().isEmpty());
  }

  private UserEntity getUserEntity() {
    UserEntity userEntity = new UserEntity();
    userEntity.setId(UUID.randomUUID());
    userEntity.setEmail("test@example.com");
    userEntity.setCreated(OffsetDateTime.now());
    return userEntity;
  }
}
