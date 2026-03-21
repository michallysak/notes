package pl.michallysak.notes.application.quarkus.note.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.note.dto.CreateNoteRequest;
import pl.michallysak.notes.application.quarkus.note.dto.NoteResponse;
import pl.michallysak.notes.application.quarkus.note.dto.NoteUpdateRequest;
import pl.michallysak.notes.application.quarkus.note.mapper.NoteMapper;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.service.NoteService;
import pl.michallysak.notes.user.service.CurrentUserProvider;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteControllerTest {
    private final static UUID AUTHOR_ID = UUID.randomUUID();
    @Mock
    NoteService noteService;
    @Mock
    NoteMapper noteMapper;
    @Mock
    CurrentUserProvider currentUserProvider;
    @InjectMocks
    NoteController noteController;

    @Test
    void createNote_shouldMapAndDelegate() {
        // given
        CreateNoteRequest request = mock(CreateNoteRequest.class);
        CreateNote createNote = mock(CreateNote.class);
        NoteValue noteValue = mock(NoteValue.class);
        NoteResponse response = mock(NoteResponse.class);
        when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
        when(noteMapper.mapToCreateNote(any(CreateNoteRequest.class), eq(AUTHOR_ID))).thenReturn(createNote);
        when(noteService.createNote(createNote)).thenReturn(noteValue);
        when(noteMapper.mapToNoteResponse(noteValue)).thenReturn(response);
        // when
        NoteResponse result = noteController.createNote(request);
        // then
        assertEquals(response, result);
        verify(noteMapper).mapToCreateNote(request, AUTHOR_ID);
        verify(noteService).createNote(createNote);
        verify(noteMapper).mapToNoteResponse(noteValue);
    }

    @Test
    void getNotes_shouldReturnMappedList() {
        // given
        NoteValue noteValue1 = mock(NoteValue.class);
        NoteValue noteValue2 = mock(NoteValue.class);
        NoteResponse response1 = mock(NoteResponse.class);
        NoteResponse response2 = mock(NoteResponse.class);
        List<NoteValue> noteValues = Arrays.asList(noteValue1, noteValue2);
        when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
        when(noteService.getCreatedNotes(AUTHOR_ID)).thenReturn(noteValues);
        when(noteMapper.mapToNoteResponse(noteValue1)).thenReturn(response1);
        when(noteMapper.mapToNoteResponse(noteValue2)).thenReturn(response2);
        // when
        List<NoteResponse> result = noteController.getNotes();
        // then
        assertEquals(2, result.size());
        assertTrue(result.contains(response1));
        assertTrue(result.contains(response2));
        verify(noteService).getCreatedNotes(AUTHOR_ID);
        verify(noteMapper).mapToNoteResponse(noteValue1);
        verify(noteMapper).mapToNoteResponse(noteValue2);
    }

    @Test
    void getNote_shouldMapAndDelegate() {
        // given
        UUID id = UUID.randomUUID();
        NoteValue noteValue = mock(NoteValue.class);
        NoteResponse response = mock(NoteResponse.class);
        when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
        when(noteService.getCreatedNote(id, AUTHOR_ID)).thenReturn(noteValue);
        when(noteMapper.mapToNoteResponse(noteValue)).thenReturn(response);
        // when
        NoteResponse result = noteController.getNote(id);
        // then
        assertEquals(response, result);
        verify(noteService).getCreatedNote(id, AUTHOR_ID);
        verify(noteMapper).mapToNoteResponse(noteValue);
    }

    @Test
    void updateNote_shouldMapAndDelegate() {
        // given
        UUID id = UUID.randomUUID();
        NoteUpdateRequest request = mock(NoteUpdateRequest.class);
        NoteUpdate noteUpdate = mock(NoteUpdate.class);
        NoteValue noteValue = mock(NoteValue.class);
        NoteResponse response = mock(NoteResponse.class);
        when(currentUserProvider.getCurrentUserId()).thenReturn(AUTHOR_ID);
        when(noteMapper.mapToNoteUpdate(request, AUTHOR_ID)).thenReturn(noteUpdate);
        when(noteService.updateNote(id, noteUpdate)).thenReturn(noteValue);
        when(noteMapper.mapToNoteResponse(noteValue)).thenReturn(response);
        // when
        NoteResponse result = noteController.updateNote(id, request);
        // then
        assertEquals(response, result);
        verify(noteMapper).mapToNoteUpdate(request, AUTHOR_ID);
        verify(noteService).updateNote(id, noteUpdate);
        verify(noteMapper).mapToNoteResponse(noteValue);
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
}

