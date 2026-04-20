package pl.michallysak.notes.application.quarkus.note.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.michallysak.notes.user.repository.UserEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notes")
@NoArgsConstructor
public class NoteEntity extends PanacheEntityBase {
  @Id private UUID id;

  @ManyToOne
  @JoinColumn(name = "author_id", nullable = false)
  private UserEntity author;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(nullable = false)
  private OffsetDateTime created;

  private OffsetDateTime updated;

  @Column(nullable = false)
  private boolean pinned;
}
