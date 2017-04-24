package io.arlas.server.rest;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    Logger logger = LoggerFactory.getLogger(ArlasExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException e) {
        logger.error("Error occurred", e);
        return ResponseFormatter.getErrorResponse(e, Response.Status.BAD_REQUEST,
                "Invalid JSON parameter. Fields indexName and typeName are mandatory.");
    }
}
