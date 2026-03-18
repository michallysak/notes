package pl.michallysak.notes.note;

import pl.michallysak.notes.note.model.CreateNote;
import pl.michallysak.notes.note.model.NoteUpdate;
import pl.michallysak.notes.note.model.NoteValue;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public class NoteTestUtils {

    private final static UUID AUTHOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    public static CreateNote.CreateNoteBuilder createCreateNoteBuilder() {
        return CreateNote.builder()
                .title("validTitle")
                .content("validContent")
                .authorId(AUTHOR_ID);
    }

    public static NoteUpdate.NoteUpdateBuilder createNoteUpdateBuilder() {
        return NoteUpdate.builder()
                .title("validTitle")
                .content("validContent")
                .pinned(null);
    }

    public static NoteValue.NoteValueBuilder createNoteValueBuilder() {
        return NoteValue.builder()
                .id(UUID.randomUUID())
                .title("validTitle")
                .content("validContent")
                .created(OffsetDateTime.now())
                .updated(Optional.empty())
                .pinned(true);
    }

}
