package pl.michallysak.notes.note.domain;

import pl.michallysak.notes.note.model.NoteUpdate;
import java.util.UUID;

public interface NoteActions {
    void read(UUID actingUserId);
    void update(NoteUpdate noteUpdate);
    void delete(UUID actingUserId);
}
