package pl.michallysak.notes.application.quarkus.note.attachment.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Schema(description = "Attachment meta response")
public class AttachmentResponse {
  @Schema(description = "Attachment id")
  private UUID id;

  @Schema(description = "Note id this attachment belongs to")
  private UUID noteId;

  @Schema(description = "Author id who uploaded the attachment")
  private UUID authorId;

  @Schema(description = "Filename of the attachment", examples = "image.png")
  private String fileName;

  @Schema(description = "Content type of the attachment", examples = "image/png")
  private String contentType;

  @Schema(description = "Size of the attachment in bytes")
  private long size;

  @Schema(description = "Creation timestamp of the attachment")
  private OffsetDateTime created;
}
