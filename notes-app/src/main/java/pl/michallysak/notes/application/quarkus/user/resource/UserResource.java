package pl.michallysak.notes.application.quarkus.user.resource;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import pl.michallysak.notes.application.quarkus.common.dto.ErrorResponse;
import pl.michallysak.notes.application.quarkus.common.openapi.OpenApiConfig;
import pl.michallysak.notes.application.quarkus.user.controller.UserController;
import pl.michallysak.notes.application.quarkus.user.dto.AuthTokenResponse;
import pl.michallysak.notes.application.quarkus.user.dto.LoginUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.RegisterUserRequest;
import pl.michallysak.notes.application.quarkus.user.dto.UserResponse;

@Tag(name = "Users API", description = "Operations on users")
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@RequiredArgsConstructor
public class UserResource {
  private final UserController userController;

  @POST
  @Path("/register")
  @Operation(
      summary = "Register a new user",
      operationId = "registerUser",
      description = "Registers a new user with email and password")
  @APIResponse(
      responseCode = "201",
      description = "User registered",
      content = @Content(schema = @Schema(implementation = AuthTokenResponse.class)))
  @APIResponse(
      responseCode = "400",
      description = "Invalid request",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public AuthTokenResponse register(RegisterUserRequest request) {
    return userController.register(request);
  }

  @POST
  @Path("/login")
  @Operation(
      summary = "Login user",
      operationId = "loginUser",
      description = "Logs in a user with email and password")
  @APIResponse(
      responseCode = "201",
      description = "User logged in",
      content = @Content(schema = @Schema(implementation = AuthTokenResponse.class)))
  @APIResponse(
      responseCode = "401",
      description = "Invalid credential",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @APIResponse(
      responseCode = "400",
      description = "Invalid request",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public AuthTokenResponse login(LoginUserRequest request) {
    return userController.login(request);
  }

  @GET
  @Path("/me")
  @Operation(
      summary = "Get current user info",
      operationId = "getCurrentUser",
      description = "Returns information about the current user")
  @APIResponse(
      responseCode = "200",
      description = "Current user info",
      content = @Content(schema = @Schema(implementation = UserResponse.class)))
  @APIResponse(
      responseCode = "401",
      description = "Not authorized",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @APIResponse(
      responseCode = "404",
      description = "User not found",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @Authenticated
  @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
  public UserResponse me() {
    return userController.me();
  }
}
