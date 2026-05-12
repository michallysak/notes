package pl.michallysak.notes.application.quarkus.note.controller;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
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

@ApplicationScoped
@RequiredArgsConstructor
public class NoteController {
  private final NoteService noteService;
  private final NoteMapper noteMapper;
  private final CurrentUserProvider currentUserProvider;
  private final UserService userService;

  public NoteResponse createNote(CreateNoteRequest request) {
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    CreateNote createNote = noteMapper.mapToCreateNote(request, currentUserId);
    NoteValue noteValue = noteService.createNote(createNote);
    return noteMapper.mapToNoteResponse(noteValue);
  }

  public List<NoteResponse> getNotes() {
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    return noteService.getCreatedNotes(currentUserId).stream()
        .map(noteMapper::mapToNoteResponse)
        .toList();
  }

  public PagedResponse<NoteResponse> searchNotes(NotePagedQuery query) {
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    Paged<NoteValue> searchResult = noteService.search(currentUserId, query);
    List<NoteResponse> data =
        searchResult.data().stream().map(noteMapper::mapToNoteResponse).toList();
    return new PagedResponse<>(
        data, searchResult.page(), searchResult.size(), searchResult.total());
  }

  public NoteResponse getNote(UUID id) {
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    NoteValue noteValue = noteService.getCreatedNote(id, currentUserId);
    return noteMapper.mapToNoteResponse(noteValue);
  }

  public NoteResponse updateNote(UUID id, NoteUpdateRequest request) {
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    NoteUpdate noteUpdate = noteMapper.mapToNoteUpdate(request, currentUserId);
    NoteValue noteValue = noteService.updateNote(id, noteUpdate);
    return noteMapper.mapToNoteResponse(noteValue);
  }

  public void deleteNote(UUID id) {
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    noteService.deleteNote(id, currentUserId);
  }

  public void setPermissions(UUID id, SetNotePermissionsRequest request) {
    Email email = Email.of(request.getEmail());
    UserValue targetUser = userService.getUserByEmail(email);
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    SetNotePermissions setPermissions =
        new SetNotePermissions(targetUser.id(), request.getPermissions());
    noteService.setPermissions(id, currentUserId, setPermissions);
  }

  public void removeAccess(UUID id, UUID targetUserId) {
    userService.getUser(targetUserId);
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    noteService.removeAccess(id, currentUserId, targetUserId);
  }

  public List<NoteShareResponse> getPermissions(UUID id) {
    UUID currentUserId = currentUserProvider.getCurrentUserId();
    Set<NoteShare> shares = noteService.getPermissions(id, currentUserId);
    return shares.stream().map(this::mapToNoteShareResponse).toList();
  }

  private NoteShareResponse mapToNoteShareResponse(NoteShare noteShare) {
    Email email = userService.getUser(noteShare.userId()).email();
    return noteMapper.mapToNoteShareResponse(noteShare, email);
  }
}
