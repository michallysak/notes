package pl.michallysak.notes.application.quarkus.note.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.michallysak.notes.note.model.NotePermission;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "note_shares")
@NoArgsConstructor
public class NotePublicShareEntity extends PanacheEntityBase {
  @Id private UUID id;

  @ManyToOne
  @JoinColumn(name = "note_id", nullable = false)
  private NoteEntity note;

  @ElementCollection(fetch = FetchType.EAGER)
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private Set<NotePermission> permissions;
}
