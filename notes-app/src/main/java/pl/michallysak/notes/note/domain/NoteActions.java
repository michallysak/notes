package pl.michallysak.notes.note.domain;

import pl.michallysak.notes.note.model.NoteUpdate;

public interface NoteActions {
    void update(NoteUpdate noteUpdate);
}
