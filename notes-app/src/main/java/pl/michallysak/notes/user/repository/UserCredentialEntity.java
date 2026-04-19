package pl.michallysak.notes.user.repository;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_credentials")
@NoArgsConstructor
public class UserCredentialEntity extends PanacheEntityBase {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  private OffsetDateTime created;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String value;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private UserEntity user;
}
