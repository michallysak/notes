package pl.michallysak.notes.note.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.note.NoteTestUtils;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;
import pl.michallysak.notes.note.validator.NoteValidator;

@ExtendWith(MockitoExtension.class)
class NoteValueTest {

  @Mock private NoteValidator noteValidator;

  @Test
  void from_shouldMapNoteFieldsCorrectly() {
    // given
    CreateNote createNote = NoteTestUtils.createCreateNoteBuilder().build();
    Note note = new NoteImpl(createNote, noteValidator);
    // when
    NoteValue value = NoteValue.from(note);
    // then
    assertEquals(note.getId(), value.id());
    assertEquals(note.getAuthorId(), value.authorId());
    assertEquals(note.getTitle(), value.title());
    assertEquals(note.getContent(), value.content());
    assertEquals(note.getCreated(), value.created());
    assertEquals(note.getUpdated(), value.updated());
    assertEquals(note.isPinned(), value.pinned());
  }
}
