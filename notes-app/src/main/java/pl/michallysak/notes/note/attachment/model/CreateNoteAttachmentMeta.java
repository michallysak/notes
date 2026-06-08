package pl.michallysak.notes.note.attachment.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateNoteAttachmentMeta(
    UUID noteId, UUID authorId, String fileName, String contentType, long size) {}
