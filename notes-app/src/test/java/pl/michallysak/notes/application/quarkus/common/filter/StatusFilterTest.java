package pl.michallysak.notes.application.quarkus.common.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatusFilterTest {
    @InjectMocks
    private StatusFilter statusFilter;
    @Mock
    private ContainerRequestContext requestContext;
    @Mock
    private ContainerResponseContext responseContext;

    @Test
    void filter_shouldNotChangeStatus_whenNot200Status() {
        // given
        when(responseContext.getStatus()).thenReturn(418);
        // when
        statusFilter.filter(requestContext, responseContext);
        // then
        verify(responseContext, never()).setStatus(anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "PATCH", "HEAD", "OPTIONS", "TRACE"})
    void filter_shouldNotChangeStatus_when200StatusAndNotMappedHttpMethod(String method) {
        // given
        when(requestContext.getMethod()).thenReturn(method);
        when(responseContext.getStatus()).thenReturn(200);
        // when
        statusFilter.filter(requestContext, responseContext);
        // then
        verify(responseContext, never()).setStatus(anyInt());
    }

    @Test
    void filter_shouldSetNoContentStatus_when200StatusAndMethodDelete() {
        // given
        when(requestContext.getMethod()).thenReturn("DELETE");
        when(responseContext.getStatus()).thenReturn(200);
        // when
        statusFilter.filter(requestContext, responseContext);
        // then
        verify(responseContext).setStatus(204);
    }

    @Test
    void filter_shouldSetCreatedStatus_when200StatusAndMethodPost() {
        // given
        when(requestContext.getMethod()).thenReturn("POST");
        when(responseContext.getStatus()).thenReturn(200);
        // when
        statusFilter.filter(requestContext, responseContext);
        // then
        verify(responseContext).setStatus(201);
    }

}
