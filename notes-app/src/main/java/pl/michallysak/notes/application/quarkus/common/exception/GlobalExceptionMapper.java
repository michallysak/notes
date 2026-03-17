package pl.michallysak.notes.application.quarkus.common.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import pl.michallysak.notes.application.quarkus.common.dto.ErrorResponse;
import pl.michallysak.notes.common.exception.EntityNotFoundException;
import pl.michallysak.notes.common.exception.ValidationException;


@Provider
@RequiredArgsConstructor
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private final Logger logger;

    @Override
    public Response toResponse(Throwable exception) {
        return switch (exception) {
            case EntityNotFoundException e -> notFoundResponse(e);
            case ValidationException e -> badReqestResponse(e);
            default -> internalServerError(exception);
        };
    }

    private Response internalServerError(Throwable exception) {
        logger.error("Unexpected exception occurred: ", exception);
        return getBuild(Response.Status.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
    }

    private Response badReqestResponse(ValidationException exception) {
        return getBuild(Response.Status.BAD_REQUEST, exception);
    }

    private Response notFoundResponse(Exception exception) {
        return getBuild(Response.Status.NOT_FOUND, exception);
    }

    private static Response getBuild(Response.Status httpStatus, Throwable exception) {
        return getBuild(httpStatus, exception.getMessage());
    }

    private static Response getBuild(Response.Status httpStatus, String message) {
        return Response
                .status(httpStatus)
                .entity(new ErrorResponse(message))
                .build();
    }

}
