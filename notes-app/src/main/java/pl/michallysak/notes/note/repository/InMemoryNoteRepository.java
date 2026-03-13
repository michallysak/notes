package pl.michallysak.notes.note.repository;

import pl.michallysak.notes.note.domain.Note;

import java.util.*;

public class InMemoryNoteRepository implements NoteRepository {
    private final Map<UUID, Note> notes;

    public InMemoryNoteRepository() {
        notes = new HashMap<>();
    }

    public InMemoryNoteRepository(Map<UUID, Note> initialNotes) {
        this.notes = new HashMap<>(initialNotes);
    }

    @Override
    public void save(Note note) {
        notes.put(note.getId(), note);
    }

    @Override
    public List<Note> findAll() {
        return notes.values().stream().toList();
    }

    @Override
    public Optional<Note> findById(UUID id) {
        return Optional.ofNullable(notes.get(id));
    }

    @Override
    public boolean deleteById(UUID id) {
        return notes.remove(id) != null;
    }

}

