package pl.michallysak.notes.note.model;

import java.util.Set;
import java.util.UUID;

public record SetNotePermissions(UUID targetUserId, Set<NotePermission> permissions) {}
