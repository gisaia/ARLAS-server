package io.arlas.server.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


@Provider
@PreMatching
public class InsensitiveCaseFilter implements ContainerRequestFilter {
    Logger LOGGER = LoggerFactory.getLogger(InsensitiveCaseFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            toLowerCaseKeyQueryParams(requestContext);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Replace the existing query parameters with lowercase key
     *
     * @param request
     */
    private void toLowerCaseKeyQueryParams(ContainerRequestContext request) throws URISyntaxException {
        UriBuilder builder = request.getUriInfo().getRequestUriBuilder();
        MultivaluedMap<String, String> queries = request.getUriInfo().getQueryParameters();
        String newRequest = request.getUriInfo().getRequestUri().normalize().toString();
        for (String key : queries.keySet()) {
            newRequest = newRequest.replace("&" + key + "=", "&" + key.toLowerCase() + "=");
            newRequest = newRequest.replace("?" + key + "=", "?" + key.toLowerCase() + "=");

        }
        URI myURI = new URI(newRequest);
        request.setRequestUri(myURI);
    }
}
