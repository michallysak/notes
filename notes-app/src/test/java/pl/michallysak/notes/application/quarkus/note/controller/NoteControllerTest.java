package pl.michallysak.notes.application.quarkus.note.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.common.SortList;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteQueryBean;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteShareResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;
import pl.michallysak.notes.application.quarkus.note.dto.PagedResponse;
import pl.michallysak.notes.application.quarkus.note.dto.SetNotePermissionsRequest;
import pl.michallysak.notes.application.quarkus.note.mapper.NoteMapper;
import pl.michallysak.notes.common.Email;
import pl.michallysak.notes.note.model.*;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.user.model.UserValue;
import pl.michallysak.notes.user.service.CurrentUserProvider;
import pl.michallysak.notes.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class NoteControllerTest {
  private static final UUID AUTHOR_ID = UUID.randomUUID();
  @Mock NoteService noteService;
  @Mock NoteMapper noteMapper;
  @Mock CurrentUserProvider currentUserProvider;
  @Mock UserService userService;
  @InjectMocks NoteController noteController;

  @Test
  void createNote_shouldMapAndDelegate() {
    // given
    CreateNoteRequest request = mock(CreateNoteRequest.class);
    CreateNote createNote = mock(CreateNote.class);
    NoteValue noteValue = mock(NoteValue.class);
    NoteResponse response = mock(NoteResponse.class);
    when(noteValue.shares()).thenReturn(Set.of());
    when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
    when(noteMapper.mapToCreateNote(any(CreateNoteRequest.class), eq(AUTHOR_ID)))
        .thenReturn(createNote);
    when(noteService.createNote(createNote)).thenReturn(noteValue);
    when(noteMapper.mapToNoteResponse(eq(noteValue), anyList())).thenReturn(response);
    // when
    NoteResponse result = noteController.createNote(request);
    // then
    assertEquals(response, result);
    verify(noteMapper).mapToCreateNote(request, AUTHOR_ID);
    verify(noteService).createNote(createNote);
    verify(noteMapper).mapToNoteResponse(eq(noteValue), anyList());
  }

  @Test
  void getNotes_shouldReturnMappedList() {
    // given
    NoteValue noteValue1 = mock(NoteValue.class);
    NoteValue noteValue2 = mock(NoteValue.class);
    NoteResponse response1 = mock(NoteResponse.class);
    NoteResponse response2 = mock(NoteResponse.class);
    List<NoteValue> noteValues = Arrays.asList(noteValue1, noteValue2);
    when(noteValue1.shares()).thenReturn(Set.of());
    when(noteValue2.shares()).thenReturn(Set.of());
    when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
    when(noteService.getCreatedNotes(AUTHOR_ID)).thenReturn(noteValues);
    when(noteMapper.mapToNoteResponse(eq(noteValue1), anyList())).thenReturn(response1);
    when(noteMapper.mapToNoteResponse(eq(noteValue2), anyList())).thenReturn(response2);
    // when
    List<NoteResponse> result = noteController.getNotes();
    // then
    assertEquals(2, result.size());
    assertTrue(result.contains(response1));
    assertTrue(result.contains(response2));
    verify(noteService).getCreatedNotes(AUTHOR_ID);
    verify(noteMapper).mapToNoteResponse(eq(noteValue1), anyList());
    verify(noteMapper).mapToNoteResponse(eq(noteValue2), anyList());
  }

  @Test
  void searchNotes_shouldReturnMappedPagedResponse() {
    // given
    NoteValue noteValue = mock(NoteValue.class);
    NoteResponse response = mock(NoteResponse.class);
    NoteQueryBean query = new NoteQueryBean(true, null, 1, 10, SortList.empty());
    when(noteValue.shares()).thenReturn(Set.of());
    when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
    when(noteService.search(AUTHOR_ID, query))
        .thenReturn(new Paged<>(List.of(noteValue), 1, 10, 20));
    when(noteMapper.mapToNoteResponse(eq(noteValue), anyList())).thenReturn(response);
    // when
    PagedResponse<NoteResponse> result = noteController.searchNotes(query);
    // then
    assertEquals(1, result.getData().size());
    assertEquals(response, result.getData().getFirst());
    assertEquals(1, result.getPage());
    assertEquals(10, result.getSize());
    assertEquals(20, result.getTotal());
    verify(noteService).search(AUTHOR_ID, query);
    verify(noteMapper).mapToNoteResponse(eq(noteValue), anyList());
  }

  @Test
  void getNote_shouldMapAndDelegate() {
    // given
    UUID id = UUID.randomUUID();
    NoteValue noteValue = mock(NoteValue.class);
    NoteResponse response = mock(NoteResponse.class);
    when(noteValue.shares()).thenReturn(Set.of());
    when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
    when(noteService.getCreatedNote(id, AUTHOR_ID)).thenReturn(noteValue);
    when(noteMapper.mapToNoteResponse(eq(noteValue), anyList())).thenReturn(response);
    // when
    NoteResponse result = noteController.getNote(id);
    // then
    assertEquals(response, result);
    verify(noteService).getCreatedNote(id, AUTHOR_ID);
    verify(noteMapper).mapToNoteResponse(eq(noteValue), anyList());
  }

  @Test
  void updateNote_shouldMapAndDelegate() {
    // given
    UUID id = UUID.randomUUID();
    NoteUpdateRequest request = mock(NoteUpdateRequest.class);
    NoteUpdate noteUpdate = mock(NoteUpdate.class);
    NoteValue noteValue = mock(NoteValue.class);
    NoteResponse response = mock(NoteResponse.class);
    when(noteValue.shares()).thenReturn(Set.of());
    when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
    when(noteMapper.mapToNoteUpdate(request, AUTHOR_ID)).thenReturn(noteUpdate);
    when(noteService.updateNote(id, noteUpdate)).thenReturn(noteValue);
    when(noteMapper.mapToNoteResponse(eq(noteValue), anyList())).thenReturn(response);
    // when
    NoteResponse result = noteController.updateNote(id, request);
    // then
    assertEquals(response, result);
    verify(noteMapper).mapToNoteUpdate(request, AUTHOR_ID);
    verify(noteService).updateNote(id, noteUpdate);
    verify(noteMapper).mapToNoteResponse(eq(noteValue), anyList());
  }

  @Test
  void deleteNote_shouldDelegate() {
    // given
    UUID id = UUID.randomUUID();
    when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
    // when
    noteController.deleteNote(id);
    // then
    verify(noteService).deleteNote(id, AUTHOR_ID);
  }

  @Test
  void setPermissions_shouldDelegate() {
    // given
    UUID noteId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    Email targetEmail = Email.of("shared@example.com");
    SetNotePermissionsRequest request =
        SetNotePermissionsRequest.builder()
            .email(targetEmail.getValue())
            .permissions(Set.of(NotePermission.READ))
            .build();
    SetNotePermissions serviceRequest =
        new SetNotePermissions(targetUserId, request.getPermissions());
    when(userService.getUserByEmail(targetEmail))
        .thenReturn(UserValue.builder().id(targetUserId).email(targetEmail).build());
    when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
    // when
    noteController.setPermissions(noteId, request);
    // then
    verify(userService).getUserByEmail(targetEmail);
    verify(noteService).setPermissions(noteId, AUTHOR_ID, serviceRequest);
  }

  @Test
  void removeAccess_shouldDelegate() {
    // given
    UUID noteId = UUID.randomUUID();
    UUID targetUserId = UUID.randomUUID();
    when(userService.getUser(targetUserId)).thenReturn(null);
    when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
    // when
    noteController.removeAccess(noteId, targetUserId);
    // then
    verify(userService).getUser(targetUserId);
    verify(noteService).removeAccess(noteId, AUTHOR_ID, targetUserId);
  }

  @Test
  void getPermissions_shouldReturnMappedShares_whenAuthor() {
    // given
    UUID noteId = UUID.randomUUID();
    UUID sharedUserId = UUID.randomUUID();
    Email sharedUserEmail = Email.of("shared@example.com");
    NoteShare noteShare = new NoteShare(sharedUserId, Set.of(NotePermission.READ));
    NoteShareResponse mappedResponse =
        NoteShareResponse.builder()
            .userId(sharedUserId)
            .email(sharedUserEmail.getValue())
            .permissions(Set.of(NotePermission.READ))
            .build();
    when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
    when(noteService.getPermissions(noteId, AUTHOR_ID)).thenReturn(Set.of(noteShare));
    when(noteMapper.mapToNoteShareResponse(noteShare, sharedUserEmail)).thenReturn(mappedResponse);
    when(userService.getUser(sharedUserId))
        .thenReturn(UserValue.builder().id(sharedUserId).email(sharedUserEmail).build());
    // when
    List<NoteShareResponse> result = noteController.getPermissions(noteId);
    // then
    assertEquals(1, result.size());
    assertEquals(sharedUserId, result.getFirst().getUserId());
    assertEquals(sharedUserEmail.getValue(), result.getFirst().getEmail());
    assertEquals(Set.of(NotePermission.READ), result.getFirst().getPermissions());
    verify(noteService).getPermissions(noteId, AUTHOR_ID);
    verify(noteMapper).mapToNoteShareResponse(noteShare, sharedUserEmail);
    verify(userService).getUser(sharedUserId);
  }

  @Test
  void getPermissions_shouldReturnOnlyCurrentUserShare_whenNotAuthor() {
    // given
    UUID noteId = UUID.randomUUID();
    UUID currentUserId = UUID.randomUUID();
    Email currentUserEmail = Email.of("current@example.com");
    NoteShare currentUserShare = new NoteShare(currentUserId, Set.of(NotePermission.READ));
    NoteShareResponse mappedResponse =
        NoteShareResponse.builder()
            .userId(currentUserId)
            .email(currentUserEmail.getValue())
            .permissions(Set.of(NotePermission.READ))
            .build();
    when(currentUserProvider.getCurrentUserId()).thenReturn(currentUserId);
    when(noteService.getPermissions(noteId, currentUserId)).thenReturn(Set.of(currentUserShare));
    when(noteMapper.mapToNoteShareResponse(currentUserShare, currentUserEmail))
        .thenReturn(mappedResponse);
    when(userService.getUser(currentUserId))
        .thenReturn(UserValue.builder().id(currentUserId).email(currentUserEmail).build());
    // when
    List<NoteShareResponse> result = noteController.getPermissions(noteId);
    // then
    assertEquals(1, result.size());
    assertEquals(currentUserId, result.getFirst().getUserId());
    assertEquals(currentUserEmail.getValue(), result.getFirst().getEmail());
    assertEquals(Set.of(NotePermission.READ), result.getFirst().getPermissions());
    verify(noteService).getPermissions(noteId, currentUserId);
    verify(noteMapper).mapToNoteShareResponse(currentUserShare, currentUserEmail);
    verify(userService).getUser(currentUserId);
  }
}
