package pl.michallysak.notes.application.quarkus.note.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.note.mapper.NoteMapper;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.model.FieldSort;
import pl.michallysak.notes.note.model.NotePagedQuery;
import pl.michallysak.notes.note.model.SortDirection;

@ExtendWith(MockitoExtension.class)
class PanacheNoteRepositoryTest {

  @Mock private NoteMapper noteMapper;
  @Mock private EntityManager entityManager;

  private PanacheNoteRepository noteRepository;

  @BeforeEach
  void setup() {
    noteRepository = spy(new PanacheNoteRepository(noteMapper));
  }

  @Test
  void findNotes_shouldReturnEmptyList_whenNoNotes() {
    // given
    doReturn(List.of()).when(noteRepository).listAll();
    // when
    List<Note> notes = noteRepository.findNotes();
    // then
    assertTrue(notes.isEmpty());
  }

  @Test
  void findNoteWithId_shouldReturnEmpty_whenNoExists() {
    // given
    UUID randomId = UUID.randomUUID();
    doReturn(null).when(noteRepository).findById(randomId);
    // when
    Optional<Note> note = noteRepository.findNoteWithId(randomId);
    // then
    assertTrue(note.isEmpty());
  }

  @Test
  void findNoteWithId_shouldReturnNote_whenExists() {
    // given
    UUID id = UUID.randomUUID();
    NoteEntity noteEntity = new NoteEntity();
    Note mappedNote = mock(Note.class);
    doReturn(noteEntity).when(noteRepository).findById(id);
    when(noteMapper.mapToDomain(noteEntity)).thenReturn(mappedNote);
    // when
    Optional<Note> note = noteRepository.findNoteWithId(id);
    // then
    assertTrue(note.isPresent());
    assertEquals(mappedNote, note.orElseThrow());
  }

  @Test
  void save_Note_shouldPersistenceNote() {
    // given
    Note note = mock(Note.class);
    NoteEntity noteEntity = new NoteEntity();
    when(noteMapper.mapToEntity(note)).thenReturn(noteEntity);
    doReturn(entityManager).when(noteRepository).getEntityManager();
    when(entityManager.merge(noteEntity)).thenReturn(noteEntity);
    // when
    noteRepository.saveNote(note);
    // then
    verify(noteMapper).mapToEntity(note);
    verify(entityManager).merge(noteEntity);
  }

  @Test
  void findAll_shouldReturnNotesNotes() {
    // given
    NoteEntity entity1 = new NoteEntity();
    NoteEntity entity2 = new NoteEntity();
    Note note1 = mock(Note.class);
    Note note2 = mock(Note.class);
    doReturn(List.of(entity1, entity2)).when(noteRepository).listAll();
    when(noteMapper.mapToDomain(entity1)).thenReturn(note1);
    when(noteMapper.mapToDomain(entity2)).thenReturn(note2);
    // when
    List<Note> notes = noteRepository.findNotes();
    // then
    assertEquals(2, notes.size());
    assertEquals(List.of(note1, note2), notes);
  }

  @Test
  void delete_shouldRemoveNoteAndReturnTrue_whenExists() {
    // given
    UUID id = UUID.randomUUID();
    doReturn(true).when(noteRepository).deleteById(id);
    // when
    boolean deleted = noteRepository.deleteNoteWithId(id);
    // then
    assertTrue(deleted);
  }

  @Test
  void delete_shouldRemoveNoteAndReturnFalse_whenNotExists() {
    // given
    UUID id = UUID.randomUUID();
    doReturn(false).when(noteRepository).deleteById(id);
    // when
    boolean deleted = noteRepository.deleteNoteWithId(id);
    // then
    assertFalse(deleted);
  }

  @Test
  void update_shouldModifyNote() {
    // given
    Note note = mock(Note.class);
    NoteEntity noteEntity = new NoteEntity();
    when(noteMapper.mapToEntity(note)).thenReturn(noteEntity);
    doReturn(entityManager).when(noteRepository).getEntityManager();
    when(entityManager.merge(noteEntity)).thenReturn(noteEntity);
    // when
    noteRepository.saveNote(note);
    noteRepository.saveNote(note);
    // then
    verify(entityManager, times(2)).merge(noteEntity);
  }

  @Test
  void findNotesWithAuthor_shouldReturnOnlyNotesWithGivenAuthor() {
    // given
    UUID authorId = UUID.randomUUID();
    NoteEntity entity1 = new NoteEntity();
    NoteEntity entity2 = new NoteEntity();
    Note note1 = mock(Note.class);
    Note note2 = mock(Note.class);
    doReturn(List.of(entity1, entity2)).when(noteRepository).list("author.id", authorId);
    when(noteMapper.mapToDomain(entity1)).thenReturn(note1);
    when(noteMapper.mapToDomain(entity2)).thenReturn(note2);
    // when
    List<Note> notes = noteRepository.findNotesWithAuthor(authorId);
    // then
    assertEquals(2, notes.size());
    assertEquals(List.of(note1, note2), notes);
  }

  @Test
  void deleteAll_shouldRemoveNotesNotes() {
    // given
    doReturn(0L).when(noteRepository).deleteAll();
    // when
    noteRepository.deleteNotes();
    // then
    verify(noteRepository).deleteAll();
  }

  @Test
  void search_shouldReturnPagedNotes_whenQueryProvided() {
    // given
    UUID authorId = UUID.randomUUID();
    NotePagedQuery query = mock(NotePagedQuery.class);
    when(query.getPage()).thenReturn(1);
    when(query.getSize()).thenReturn(10);
    when(query.getSort()).thenReturn(List.of());
    when(query.getIsShared()).thenReturn(null);
    // and
    NoteEntity entity1 = new NoteEntity();
    NoteEntity entity2 = new NoteEntity();
    Note note1 = mock(Note.class);
    Note note2 = mock(Note.class);
    // and
    PanacheQuery<NoteEntity> panacheQuery = mock(PanacheQuery.class);
    PanacheQuery<NoteEntity> pagedQuery = mock(PanacheQuery.class);
    // and
    doReturn(panacheQuery).when(noteRepository).find(anyString(), eq(authorId));
    when(panacheQuery.page(any(Page.class))).thenReturn(pagedQuery);
    when(pagedQuery.list()).thenReturn(List.of(entity1, entity2));
    // and
    when(noteMapper.mapToDomain(entity1)).thenReturn(note1);
    when(noteMapper.mapToDomain(entity2)).thenReturn(note2);

    // when
    List<Note> notes = noteRepository.search(authorId, query);

    // then
    assertEquals(2, notes.size());
    assertEquals(List.of(note1, note2), notes);
    verify(noteRepository).find("author.id = ?1 order by created desc", authorId);
  }

  @Test
  void search_shouldReturnPagedNotes_whenQueryWithSortingAndShared() {
    // given
    UUID authorId = UUID.randomUUID();
    NotePagedQuery query = mock(NotePagedQuery.class);
    when(query.getPage()).thenReturn(0);
    when(query.getSize()).thenReturn(5);
    when(query.getSort()).thenReturn(List.of(new FieldSort("title", SortDirection.DESC)));
    when(query.getIsShared()).thenReturn(true);
    // and
    NoteEntity entity = new NoteEntity();
    Note note = mock(Note.class);
    // and
    PanacheQuery<NoteEntity> panacheQuery = mock(PanacheQuery.class);
    PanacheQuery<NoteEntity> pagedQuery = mock(PanacheQuery.class);
    // and
    doReturn(panacheQuery).when(noteRepository).find(anyString(), eq(authorId));
    when(panacheQuery.page(any(Page.class))).thenReturn(pagedQuery);
    when(pagedQuery.list()).thenReturn(List.of(entity));
    // and
    when(noteMapper.mapToDomain(entity)).thenReturn(note);

    // when
    List<Note> notes = noteRepository.search(authorId, query);

    // then
    assertEquals(1, notes.size());
    assertEquals(List.of(note), notes);
    verify(noteRepository)
        .find(
            "(author.id = ?1 or exists (select s from shares s where s.userId = ?1)) and shares is not empty order by title desc",
            authorId);
  }

  @Test
  void search_shouldReturnPagedNotes_whenQueryWithIsSharedFalse() {
    // given
    UUID authorId = UUID.randomUUID();
    NotePagedQuery query = mock(NotePagedQuery.class);
    when(query.getPage()).thenReturn(0);
    when(query.getSize()).thenReturn(10);
    when(query.getSort()).thenReturn(List.of());
    when(query.getIsShared()).thenReturn(false);
    // and
    NoteEntity entity = new NoteEntity();
    Note note = mock(Note.class);
    // and
    PanacheQuery<NoteEntity> panacheQuery = mock(PanacheQuery.class);
    PanacheQuery<NoteEntity> pagedQuery = mock(PanacheQuery.class);
    // and
    doReturn(panacheQuery).when(noteRepository).find(anyString(), eq(authorId));
    when(panacheQuery.page(any(Page.class))).thenReturn(pagedQuery);
    when(pagedQuery.list()).thenReturn(List.of(entity));

    when(noteMapper.mapToDomain(entity)).thenReturn(note);

    // when
    List<Note> notes = noteRepository.search(authorId, query);

    // then
    assertEquals(1, notes.size());
    assertEquals(List.of(note), notes);
    verify(noteRepository)
        .find("author.id = ?1 and shares is empty order by created desc", authorId);
  }

  @Test
  void search_shouldReturnPagedNotes_whenQueryWithSortAsc() {
    // given
    UUID authorId = UUID.randomUUID();
    NotePagedQuery query = mock(NotePagedQuery.class);
    when(query.getPage()).thenReturn(0);
    when(query.getSize()).thenReturn(10);
    when(query.getSort()).thenReturn(List.of(new FieldSort("title", SortDirection.ASC)));
    when(query.getIsShared()).thenReturn(null);
    // and
    NoteEntity entity = new NoteEntity();
    Note note = mock(Note.class);
    // and
    PanacheQuery<NoteEntity> panacheQuery = mock(PanacheQuery.class);
    PanacheQuery<NoteEntity> pagedQuery = mock(PanacheQuery.class);
    // and
    doReturn(panacheQuery).when(noteRepository).find(anyString(), eq(authorId));
    when(panacheQuery.page(any(Page.class))).thenReturn(pagedQuery);
    when(pagedQuery.list()).thenReturn(List.of(entity));
    // and
    when(noteMapper.mapToDomain(entity)).thenReturn(note);

    // when
    List<Note> notes = noteRepository.search(authorId, query);

    // then
    assertEquals(1, notes.size());
    assertEquals(List.of(note), notes);
    verify(noteRepository).find("author.id = ?1 order by title", authorId);
  }

  @Test
  void search_shouldReturnPagedNotes_whenQueryWithSortNull() {
    // given
    UUID authorId = UUID.randomUUID();
    NotePagedQuery query = mock(NotePagedQuery.class);
    when(query.getPage()).thenReturn(0);
    when(query.getSize()).thenReturn(10);
    when(query.getSort()).thenReturn(null);
    when(query.getIsShared()).thenReturn(null);
    // and
    NoteEntity entity = new NoteEntity();
    Note note = mock(Note.class);
    // and
    PanacheQuery<NoteEntity> panacheQuery = mock(PanacheQuery.class);
    PanacheQuery<NoteEntity> pagedQuery = mock(PanacheQuery.class);
    // and
    doReturn(panacheQuery).when(noteRepository).find(anyString(), eq(authorId));
    when(panacheQuery.page(any(Page.class))).thenReturn(pagedQuery);
    when(pagedQuery.list()).thenReturn(List.of(entity));
    // and
    when(noteMapper.mapToDomain(entity)).thenReturn(note);

    // when
    List<Note> notes = noteRepository.search(authorId, query);

    // then
    assertEquals(1, notes.size());
    assertEquals(List.of(note), notes);
    verify(noteRepository).find("author.id = ?1 order by created desc", authorId);
  }
}
