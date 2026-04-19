package pl.michallysak.notes.application.quarkus.user.domain;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.application.quarkus.user.mapper.UserMapper;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.user.domain.User;
import pl.michallysak.notes.user.repository.UserEntity;
import pl.michallysak.notes.user.repository.UserRepository;

@ApplicationScoped
@Typed(PanacheUserRepository.class)
@RequiredArgsConstructor
public class PanacheUserRepository
    implements UserRepository, PanacheRepositoryBase<UserEntity, UUID> {
  private final UserMapper userMapper;

  @Override
  @Transactional
  public void saveUser(User user) {
    UserEntity entity = userMapper.mapToEntity(user);
    getEntityManager().merge(entity);
  }

  @Override
  public Optional<User> findUserWithId(UUID id) {
    return Optional.ofNullable(findById(id)).map(userMapper::mapToDomain);
  }

  @Override
  public boolean existsWithEmail(Email email) {
    return find("email", email.getValue()).firstResultOptional().isPresent();
  }

  @Override
  public Optional<User> findUserWithEmail(Email email) {
    return find("email", email.getValue()).firstResultOptional().map(userMapper::mapToDomain);
  }

  @Override
  @Transactional
  public void deleteUsers() {
    deleteAll();
  }
}
