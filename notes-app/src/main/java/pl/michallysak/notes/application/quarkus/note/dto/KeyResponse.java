package pl.michallysak.notes.application.quarkus.note.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Schema(description = "Response containing the key and its expiration time.")
public class KeyResponse {
  @Schema(required = true)
  private String key;

  @Schema(required = true)
  private Instant expiresAt;
}
