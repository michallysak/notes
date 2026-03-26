package pl.michallysak.notes.application.quarkus.note.resource;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.note.controller.NoteController;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;

@ExtendWith(MockitoExtension.class)
class NoteResourceTest {
  @Mock NoteController noteController;
  @InjectMocks NoteResource noteResource;

  @Test
  void createNote_shouldDelegateToController() {
    // given
    CreateNoteRequest request = mock(CreateNoteRequest.class);
    NoteResponse response = mock(NoteResponse.class);
    when(noteController.createNote(request)).thenReturn(response);
    // when
    noteResource.createNote(request);
    // then
    verify(noteController).createNote(request);
  }

  @Test
  void getNotes_shouldDelegateToController() {
    // given
    @SuppressWarnings("unchecked")
    List<NoteResponse> responses = mock(List.class);
    when(noteController.getNotes()).thenReturn(responses);
    // when
    noteResource.getNotes();
    // then
    verify(noteController).getNotes();
  }

  @Test
  void getNote_shouldDelegateToController() {
    // given
    UUID id = UUID.randomUUID();
    NoteResponse response = mock(NoteResponse.class);
    when(noteController.getNote(id)).thenReturn(response);
    // when
    noteResource.getNote(id);
    // then
    verify(noteController).getNote(id);
  }

  @Test
  void updateNote_shouldDelegateToController() {
    // given
    UUID id = UUID.randomUUID();
    NoteUpdateRequest request = mock(NoteUpdateRequest.class);
    NoteResponse response = mock(NoteResponse.class);
    when(noteController.updateNote(id, request)).thenReturn(response);
    // when
    noteResource.updateNote(id, request);
    // then
    verify(noteController).updateNote(id, request);
  }

  @Test
  void deleteNote_shouldDelegateToController() {
    // given
    UUID id = UUID.randomUUID();
    // when
    noteResource.deleteNote(id);
    // then
    verify(noteController).deleteNote(id);
  }
}
