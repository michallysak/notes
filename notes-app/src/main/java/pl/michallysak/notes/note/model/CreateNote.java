package pl.michallysak.notes.note.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateNote(String title, String content, UUID authorId) {}
