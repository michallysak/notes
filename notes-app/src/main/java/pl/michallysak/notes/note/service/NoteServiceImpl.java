package pl.michallysak.notes.note.service;

import lombok.RequiredArgsConstructor;
import pl.michallysak.notes.note.domain.Note;
import pl.michallysak.notes.note.domain.NoteImpl;
import pl.michallysak.notes.note.exception.NoteNotFoundException;
import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;
import pl.michallysak.notes.note.repository.NoteRepository;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    @Override
    public NoteValue createNote(CreateNote createNote) {
        Note note = NoteImpl.create(createNote);
        noteRepository.save(note);
        return NoteValue.from(note);
    }

    @Override
    public List<NoteValue> getCreatedNotes() {
        return noteRepository.findAll().stream().map(NoteValue::from).toList();
    }

    @Override
    public NoteValue getCreatedNote(UUID noteId) throws NoteNotFoundException {
        Note note = noteRepository.findById(noteId).orElseThrow(NoteNotFoundException::new);
        return NoteValue.from(note);
    }

    @Override
    public NoteValue updateNote(UUID id, NoteUpdate noteUpdate) {
        Note note = noteRepository.findById(id).orElseThrow(NoteNotFoundException::new);
        note.update(noteUpdate);
        noteRepository.save(note);
        return NoteValue.from(note);
    }

    @Override
    public void deleteNote(UUID noteId) {
        boolean deleted = noteRepository.deleteById(noteId);
        if (!deleted) {
            throw new NoteNotFoundException();
        }
    }


}
