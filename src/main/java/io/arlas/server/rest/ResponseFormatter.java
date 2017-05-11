package io.arlas.server.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.arlas.server.model.response.ArlasError;
import io.arlas.server.model.response.ArlasSuccess;

public class ResponseFormatter {

    public static Response getSuccessResponse(String message) {
        return Response.ok(new ArlasSuccess(Response.Status.OK.getStatusCode(), message))
                .type(MediaType.APPLICATION_JSON).build();
    }

    public static Response getResultResponse(Object object) {
        return Response.ok(object).type(MediaType.APPLICATION_JSON).build();
    }

    public static Response getErrorResponse(Exception e, Response.Status status, String message) {
        return Response.status(status).entity(new ArlasError(status.getStatusCode(), e.getClass().getName(), message))
                .type(MediaType.APPLICATION_JSON).build();
    }
}
