package pl.michallysak.notes.application.quarkus.note.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;
import pl.michallysak.notes.application.quarkus.note.mapper.NoteMapper;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.service.NoteService;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;
    private final NoteMapper noteMapper;
    @Named("authorId")
    private final UUID authorId;

    public NoteResponse createNote(CreateNoteRequest request) {
        CreateNote createNote = noteMapper.mapToCreateNote(request, authorId);
        NoteValue noteValue = noteService.createNote(createNote);
        return noteMapper.mapToNoteResponse(noteValue);
    }

    public List<NoteResponse> getNotes() {
        return noteService.getCreatedNotes(authorId).stream()
                .map(noteMapper::mapToNoteResponse)
                .toList();
    }

    public NoteResponse getNote(UUID id) {
        NoteValue noteValue = noteService.getCreatedNote(id, authorId);
        return noteMapper.mapToNoteResponse(noteValue);
    }

    public NoteResponse updateNote(UUID id, NoteUpdateRequest request) {
        NoteUpdate noteUpdate = noteMapper.mapToNoteUpdate(request);
        NoteValue noteValue = noteService.updateNote(id, noteUpdate);
        return noteMapper.mapToNoteResponse(noteValue);
    }

    public void deleteNote(UUID id) {
        noteService.deleteNote(id, authorId);
    }
}
