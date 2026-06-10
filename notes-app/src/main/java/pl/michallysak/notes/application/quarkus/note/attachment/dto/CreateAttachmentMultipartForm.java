package pl.michallysak.notes.application.quarkus.note.attachment.dto;

import jakarta.ws.rs.FormParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Schema(description = "Multipart form for creating an attachment")
public class CreateAttachmentMultipartForm {
  @FormParam("noteId")
  @Schema(description = "Note id this attachment belongs to", required = true)
  private String noteId;

  @FormParam("fileName")
  @Schema(description = "Filename of the attachment", required = true, examples = "image.png")
  private String fileName;

  @FormParam("contentType")
  @Schema(description = "Content type of the attachment", required = true, examples = "image/png")
  private String contentType;

  @FormParam("file")
  @Schema(description = "Binary file content", required = true)
  private byte[] file;
}
