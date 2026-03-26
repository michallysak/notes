package pl.michallysak.notes.application.quarkus.user.dto;

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
@Schema(description = "Response with user details")
public class UserResponse {
  @Schema(description = "User id", example = "b3b6c8e2-8c2e-4e2a-9b2e-8c2e4e2a9b2e")
  private UUID id;

  @Schema(description = "User email", example = "user@example.com")
  private String email;
}
