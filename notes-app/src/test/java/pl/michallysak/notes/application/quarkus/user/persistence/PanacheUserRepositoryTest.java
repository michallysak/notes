package pl.michallysak.notes.application.quarkus.user.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.user.domain.PanacheUserRepository;
import pl.michallysak.notes.application.quarkus.user.mapper.UserMapper;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.domain.User;
import pl.michallysak.notes.user.repository.UserEntity;

@ExtendWith(MockitoExtension.class)
class PanacheUserRepositoryTest {

  @Mock private UserMapper userMapper;
  @Mock private EntityManager entityManager;

  private PanacheUserRepository userRepository;

  @BeforeEach
  void setup() {
    userRepository = spy(new PanacheUserRepository(userMapper));
  }

  @Test
  void findUserWithId_shouldReturnEmpty_whenNoExists() {
    // given
    UUID randomId = UUID.randomUUID();
    doReturn(null).when(userRepository).findById(randomId);
    // when
    Optional<User> user = userRepository.findUserWithId(randomId);
    // then
    assertTrue(user.isEmpty());
  }

  @Test
  void findUserWithId_shouldReturnUser_whenExists() {
    // given
    UUID id = UUID.randomUUID();
    UserEntity userEntity = new UserEntity();
    User mappedUser = mock(User.class);
    doReturn(userEntity).when(userRepository).findById(id);
    when(userMapper.mapToDomain(userEntity)).thenReturn(mappedUser);
    // when
    Optional<User> user = userRepository.findUserWithId(id);
    // then
    assertTrue(user.isPresent());
    assertEquals(mappedUser, user.orElseThrow());
  }

  @Test
  void saveUser_shouldPersistenceUser() {
    // given
    User user = mock(User.class);
    UserEntity userEntity = new UserEntity();
    when(userMapper.mapToEntity(user)).thenReturn(userEntity);
    doReturn(entityManager).when(userRepository).getEntityManager();
    when(entityManager.merge(userEntity)).thenReturn(userEntity);
    // when
    userRepository.saveUser(user);
    // then
    verify(userMapper).mapToEntity(user);
    verify(entityManager).merge(userEntity);
  }

  @Test
  void findUserWithEmail_shouldReturnEmpty_whenNoExists() {
    // given
    Email email = Email.of("missing@example.com");
    @SuppressWarnings("unchecked")
    PanacheQuery<UserEntity> panacheQuery = mock(PanacheQuery.class);
    doReturn(panacheQuery).when(userRepository).find("email", email.getValue());
    when(panacheQuery.firstResultOptional()).thenReturn(Optional.empty());
    // when
    Optional<User> user = userRepository.findUserWithEmail(email);
    // then
    assertTrue(user.isEmpty());
    verify(userRepository).find("email", email.getValue());
    verify(panacheQuery).firstResultOptional();
  }

  @Test
  void existsWithEmail_shouldReturnTrue_whenExists() {
    // given
    Email email = Email.of("present@example.com");
    @SuppressWarnings("unchecked")
    PanacheQuery<UserEntity> panacheQuery = mock(PanacheQuery.class);
    doReturn(panacheQuery).when(userRepository).find("email", email.getValue());
    when(panacheQuery.firstResultOptional()).thenReturn(Optional.of(new UserEntity()));
    // when
    boolean exists = userRepository.existsWithEmail(email);
    // then
    assertTrue(exists);
    verify(userRepository).find("email", email.getValue());
    verify(panacheQuery).firstResultOptional();
  }

  @Test
  void existsWithEmail_shouldReturnFalse_whenNotExists() {
    // given
    Email email = Email.of("missing@example.com");
    @SuppressWarnings("unchecked")
    PanacheQuery<UserEntity> panacheQuery = mock(PanacheQuery.class);
    doReturn(panacheQuery).when(userRepository).find("email", email.getValue());
    when(panacheQuery.firstResultOptional()).thenReturn(Optional.empty());
    // when
    boolean exists = userRepository.existsWithEmail(email);
    // then
    assertFalse(exists);
    verify(userRepository).find("email", email.getValue());
    verify(panacheQuery).firstResultOptional();
  }

  @Test
  void deleteUsers_shouldRemoveUsers() {
    // given
    doReturn(0L).when(userRepository).deleteAll();
    // when
    userRepository.deleteUsers();
    // then
    verify(userRepository).deleteAll();
  }
}
