package pl.michallysak.notes.application.quarkus.note.domain;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.application.quarkus.note.mapper.NoteMapper;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.repository.NoteEntity;
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
    return list("authorId", authorId).stream().map(noteMapper::mapToDomain).toList();
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
