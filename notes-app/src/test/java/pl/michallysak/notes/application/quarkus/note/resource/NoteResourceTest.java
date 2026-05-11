package pl.michallysak.notes.application.quarkus.note.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.common.SortList;
import pl.michallysak.notes.application.quarkus.note.controller.NoteController;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteQueryBean;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteShareResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;
import pl.michallysak.notes.application.quarkus.note.dto.SetNotePermissionsRequest;

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
  void searchNotes_shouldDelegateToController() {
    // given
    NoteQueryBean query = new NoteQueryBean(true, null, 1, 25, SortList.empty());
    @SuppressWarnings("unchecked")
    List<NoteResponse> response = mock(List.class);
    when(noteController.searchNotes(query)).thenReturn(response);
    // when
    List<NoteResponse> result = noteResource.searchNotes(query);
    // then
    assertEquals(response, result);
    verify(noteController).searchNotes(query);
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

  @Test
  void setPermissions_shouldDelegateToController() {
    // given
    UUID id = UUID.randomUUID();
    SetNotePermissionsRequest request = mock(SetNotePermissionsRequest.class);
    // when
    noteResource.setPermissions(id, request);
    // then
    verify(noteController).setPermissions(id, request);
  }

  @Test
  void removeAccess_shouldDelegateToController() {
    // given
    UUID id = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    // when
    noteResource.removeAccess(id, targetUserId);
    // then
    verify(noteController).removeAccess(id, targetUserId);
  }

  @Test
  void getPermissions_shouldDelegateToController() {
    // given
    UUID id = UUID.randomUUID();
    @SuppressWarnings("unchecked")
    List<NoteShareResponse> responses = mock(List.class);
    when(noteController.getPermissions(id)).thenReturn(responses);
    // when
    List<NoteShareResponse> result = noteResource.getPermissions(id);
    // then
    assertEquals(responses, result);
    verify(noteController).getPermissions(id);
  }
}
