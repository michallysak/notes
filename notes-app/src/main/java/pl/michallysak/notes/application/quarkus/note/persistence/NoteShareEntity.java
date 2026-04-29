package pl.michallysak.notes.application.quarkus.note.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.michallysak.notes.note.model.NotePermission;
import pl.michallysak.notes.user.repository.UserEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "note_shares")
@NoArgsConstructor
public class NoteShareEntity extends PanacheEntityBase {
  @Id private UUID id;

  @ManyToOne
  @JoinColumn(name = "note_id", nullable = false)
  private NoteEntity note;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @ElementCollection(fetch = FetchType.EAGER)
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private Set<NotePermission> permissions;
}
