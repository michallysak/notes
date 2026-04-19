package pl.michallysak.notes.user.repository;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@NoArgsConstructor
public class UserEntity extends PanacheEntityBase {
  @Id private UUID id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private OffsetDateTime created;

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private List<UserCredentialEntity> credentials = new ArrayList<>();

  public void setCredentials(List<UserCredentialEntity> credentials) {
    this.credentials.clear();
    if (credentials == null) {
      return;
    }
    credentials.forEach(this::addCredential);
  }

  public void addCredential(UserCredentialEntity credential) {
    if (credential == null) {
      return;
    }
    credential.setUser(this);
    this.credentials.add(credential);
  }
}
