package pl.michallysak.notes.note.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record NoteUpdate(String title, String content, Boolean pinned, UUID actingUserId) {}
