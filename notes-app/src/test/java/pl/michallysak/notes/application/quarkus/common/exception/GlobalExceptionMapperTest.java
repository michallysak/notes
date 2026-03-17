package pl.michallysak.notes.application.quarkus.common.exception;

import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.michallysak.notes.application.quarkus.common.dto.ErrorResponse;
import pl.michallysak.notes.common.exception.EntityNotFoundException;
import pl.michallysak.notes.common.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionMapperTest {

    @Mock
    Logger logger;

    @InjectMocks
    GlobalExceptionMapper globalExceptionMapper;

    @Test
    @SuppressWarnings("resource")
    void toResponse_shouldReturnValidErrorResponse_whenNotMappedException() {
        // given
        Exception exception = new Exception("fail");
        // when
        Response response = globalExceptionMapper.toResponse(exception);
        // then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        Object entity = response.getEntity();
        // and
        assertInstanceOf(ErrorResponse.class, entity);
        ErrorResponse error = (ErrorResponse) entity;
        assertEquals("An unexpected error occurred. Please try again later.", error.getMessage());
        // and
        verify(logger).error("Unexpected exception occurred: ", exception);
    }

    @Test
    @SuppressWarnings("resource")
    void toResponse_shouldReturnValidErrorResponse_whenEntityNotFoundException() {
        // given
        EntityNotFoundException exception = new EntityNotFoundException("not found");
        // when
        Response response = globalExceptionMapper.toResponse(exception);
        // then
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        Object entity = response.getEntity();
        // and
        assertInstanceOf(ErrorResponse.class, entity);
        ErrorResponse error = (ErrorResponse) entity;
        assertEquals("not found", error.getMessage());
    }

    @Test
    @SuppressWarnings("resource")
    void toResponse_shouldReturnValidErrorResponse_whenValidationException() {
        // given
        ValidationException exception = new ValidationException("validation failed");
        // when
        Response response = globalExceptionMapper.toResponse(exception);
        // then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        Object entity = response.getEntity();
        // and
        assertInstanceOf(ErrorResponse.class, entity);
        ErrorResponse error = (ErrorResponse) entity;
        assertEquals("validation failed", error.getMessage());
    }
}
