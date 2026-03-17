package pl.michallysak.notes.application.quarkus.common.filter;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class StatusFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if (responseContext.getStatus() != Response.Status.OK.getStatusCode()) {
            return;
        }
        switch (requestContext.getMethod()) {
            case HttpMethod.DELETE -> responseContext.setStatus(Response.Status.NO_CONTENT.getStatusCode());
            case HttpMethod.POST -> responseContext.setStatus(Response.Status.CREATED.getStatusCode());
        }
    }

}
