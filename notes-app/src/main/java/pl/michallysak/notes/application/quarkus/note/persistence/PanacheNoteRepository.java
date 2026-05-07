package pl.michallysak.notes.application.quarkus.note.persistence;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.application.quarkus.note.mapper.NoteMapper;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.model.FieldSort;
import pl.michallysak.notes.note.model.NotePagedQuery;
import pl.michallysak.notes.note.model.SortDirection;
import pl.michallysak.notes.note.repository.NoteRepository;

@ApplicationScoped
@Typed(PanacheNoteRepository.class)
@RequiredArgsConstructor
public class PanacheNoteRepository
    implements NoteRepository, PanacheRepositoryBase<NoteEntity, UUID> {
  private final NoteMapper noteMapper;

  @Override
  @Transactional
  public void saveNote(Note note) {
    NoteEntity entity = noteMapper.mapToEntity(note);
    getEntityManager().merge(entity);
  }

  @Override
  public List<Note> findNotes() {
    return listAll().stream().map(noteMapper::mapToDomain).toList();
  }

  @Override
  public List<Note> findNotesWithAuthor(UUID authorId) {
    return list("author.id", authorId).stream().map(noteMapper::mapToDomain).toList();
  }

  @Override
  public List<Note> search(UUID actingUserId, NotePagedQuery query) {
    PanacheQuery<NoteEntity> panacheQuery = buildQuery(actingUserId, query);
    return panacheQuery.page(Page.of(query.getPage(), query.getSize())).list().stream()
        .map(noteMapper::mapToDomain)
        .toList();
  }

  private PanacheQuery<NoteEntity> buildQuery(UUID actingUserId, NotePagedQuery query) {
    StringBuilder queryBuilder = new StringBuilder();
    if (Boolean.TRUE.equals(query.getIsShared())) {
      queryBuilder.append(
          "(author.id = ?1 or exists (select s from shares s where s.userId = ?1)) and shares is not empty");
    } else if (Boolean.FALSE.equals(query.getIsShared())) {
      queryBuilder.append("author.id = ?1 and shares is empty");
    } else {
      queryBuilder.append("author.id = ?1");
    }

    List<FieldSort> fieldSorts = query.getSort();
    if (fieldSorts != null && !fieldSorts.isEmpty()) {
      String orderBy =
          fieldSorts.stream().map(this::formatSortField).collect(Collectors.joining(", "));
      queryBuilder.append(" order by ").append(orderBy);
    } else {
      queryBuilder.append(" order by created desc");
    }

    String queryText = queryBuilder.toString();
    return find(queryText, actingUserId);
  }

  private String formatSortField(FieldSort fs) {
    String order = fs.direction() == SortDirection.DESC ? " desc" : "";
    return fs.field() + order;
  }

  @Override
  public Optional<Note> findNoteWithId(UUID id) {
    return Optional.ofNullable(findById(id)).map(noteMapper::mapToDomain);
  }

  @Override
  @Transactional
  public boolean deleteNoteWithId(UUID id) {
    return deleteById(id);
  }

  @Override
  @Transactional
  public void deleteNotes() {
    deleteAll();
  }
}
