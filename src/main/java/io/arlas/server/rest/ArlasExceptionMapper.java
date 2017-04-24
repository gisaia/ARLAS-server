package io.arlas.server.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.NotFoundException;

@Provider
public class ArlasExceptionMapper implements ExceptionMapper<ArlasException> {
    Logger logger = LoggerFactory.getLogger(ArlasExceptionMapper.class);

    @Override
    public Response toResponse(ArlasException e) {
        logger.error("Error occurred", e);
        if (e instanceof NotFoundException)
            return ResponseFormatter.getErrorResponse(e, Response.Status.NOT_FOUND, e.getMessage());
        else
            return ResponseFormatter.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
