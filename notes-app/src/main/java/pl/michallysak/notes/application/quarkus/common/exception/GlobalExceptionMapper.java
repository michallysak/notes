package pl.michallysak.notes.application.quarkus.common.exception;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import pl.michallysak.notes.application.quarkus.common.dto.ErrorResponse;
import pl.michallysak.notes.auth.exception.AuthException;
import pl.michallysak.notes.common.exception.EntityNotFoundException;
import pl.michallysak.notes.common.exception.ValidationException;

@Provider
@ApplicationScoped
@RequiredArgsConstructor
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

  private final Logger logger;

  @Override
  public Response toResponse(Throwable throwable) {
    return switch (throwable) {
      case EntityNotFoundException exception -> notFoundResponse(exception);
      case ValidationException exception -> badReqestResponse(exception);
      case AuthException exception -> unauthorizedResponse(exception);
      default -> internalServerError(throwable);
    };
  }

  private Response notFoundResponse(Exception exception) {
    return buildResponse(Response.Status.NOT_FOUND, exception);
  }

  private Response badReqestResponse(ValidationException exception) {
    return buildResponse(Response.Status.BAD_REQUEST, exception);
  }

  private Response unauthorizedResponse(AuthException exception) {
    return buildResponse(Response.Status.UNAUTHORIZED, exception);
  }

  private Response internalServerError(Throwable throwable) {
    logger.error("Unexpected exception occurred: ", throwable);
    return buildResponse(
        Response.Status.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred. Please try again later.");
  }

  private static Response buildResponse(Response.Status httpStatus, Throwable throwable) {
    return buildResponse(httpStatus, throwable.getMessage());
  }

  private static Response buildResponse(Response.Status httpStatus, String message) {
    return Response.status(httpStatus).entity(new ErrorResponse(message)).build();
  }
}
