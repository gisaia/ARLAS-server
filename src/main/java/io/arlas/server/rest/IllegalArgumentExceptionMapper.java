package io.arlas.server.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
    Logger logger = LoggerFactory.getLogger(ArlasExceptionMapper.class);

    @Override
    public Response toResponse(IllegalArgumentException e) {
        logger.error("Error occurred", e);
        return ResponseFormatter.getErrorResponse(e, Response.Status.BAD_REQUEST, e.getMessage());
    }
}
