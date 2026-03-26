package pl.michallysak.notes.application.quarkus.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Schema(description = "Request to register a user")
public class RegisterUserRequest {
  @Schema(description = "User email", example = "user@example.com", required = true)
  private String email;

  @Schema(description = "User password", example = "StrongPassword123", required = true)
  private String password;
}
