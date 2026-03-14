package pl.michallysak.notes.note;

import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;

public class NoteTestUtils {
    public static CreateNote.CreateNoteBuilder createCreateNoteBuilder() {
        return CreateNote.builder().title("validTitle").content("validContent");
    }

    public static NoteUpdate.NoteUpdateBuilder createNoteUpdateBuilder() {
        return NoteUpdate.builder().title("validTitle").content("validContent").pinned(false);
    }
}

