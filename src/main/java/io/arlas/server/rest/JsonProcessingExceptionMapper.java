package io.arlas.server.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    Logger logger = LoggerFactory.getLogger(ArlasExceptionMapper.class);

    @Override
    public Response toResponse(JsonProcessingException e) {
        logger.error("Error occurred", e);
        return ResponseFormatter.getErrorResponse(e, Response.Status.BAD_REQUEST, "Malformed JSON parameter.");
    }
}
