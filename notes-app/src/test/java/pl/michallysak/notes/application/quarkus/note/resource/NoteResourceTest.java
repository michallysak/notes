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
import pl.michallysak.notes.application.quarkus.note.dto.*;

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
    NoteQueryBean query = new NoteQueryBean(true, null, 1, 25, SortList.empty(), null);
    PagedResponse<NoteResponse> response =
        PagedResponse.<NoteResponse>builder().data(List.of()).page(1).size(25).total(0).build();
    when(noteController.searchNotes(query)).thenReturn(response);
    // when
    PagedResponse<NoteResponse> result = noteResource.searchNotes(query);
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

  @Test
  void getPublicNote_shouldDelegateToController() {
    // given
    UUID publicShareId = UUID.randomUUID();
    NoteResponse response = mock(NoteResponse.class);
    when(noteController.getPublicNote(publicShareId)).thenReturn(response);
    // when
    NoteResponse result = noteResource.getPublicNote(publicShareId);
    // then
    assertEquals(response, result);
    verify(noteController).getPublicNote(publicShareId);
  }

  @Test
  void makeNotePublic_shouldDelegateToController() {
    // given
    UUID id = UUID.randomUUID();
    UUID publicShareId = UUID.randomUUID();
    SetNotePublicRequest request = mock(SetNotePublicRequest.class);
    when(noteController.makeNotePublic(id, request)).thenReturn(publicShareId);
    // when
    noteResource.makeNotePublic(id, request);
    // then
    verify(noteController).makeNotePublic(id, request);
  }

  @Test
  void undoNotePublic_shouldDelegateToController() {
    // given
    UUID id = UUID.randomUUID();
    // when
    noteResource.undoNotePublic(id);
    // then
    verify(noteController).undoNotePublic(id);
  }
}
