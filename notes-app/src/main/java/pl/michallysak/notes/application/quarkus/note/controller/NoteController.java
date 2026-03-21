package pl.michallysak.notes.application.quarkus.note.controller;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;
import pl.michallysak.notes.application.quarkus.note.mapper.NoteMapper;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.user.service.CurrentUserProvider;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;
    private final NoteMapper noteMapper;
    private final CurrentUserProvider currentUserProvider;

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
}
