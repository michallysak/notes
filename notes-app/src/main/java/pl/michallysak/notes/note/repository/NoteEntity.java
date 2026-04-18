package pl.michallysak.notes.note.repository;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notes")
@NoArgsConstructor
public class NoteEntity extends PanacheEntityBase {
  @Id private UUID id;

  @Column(nullable = false)
  private UUID authorId;

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
