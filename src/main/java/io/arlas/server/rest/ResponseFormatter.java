package io.arlas.server.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ResponseFormatter {
    
    private static String getSuccessResponseString(String message) {
	ObjectMapper mapper = new ObjectMapper();
	ObjectNode json = mapper.createObjectNode();
	json.put("status", "200");
	json.put("message", message);
	return json.toString();
    }
    
    private static String getErrorResponseString(int status, String errorName, String message) {
	ObjectMapper mapper = new ObjectMapper();
	ObjectNode json = mapper.createObjectNode();
	json.put("status", status);
	json.put("error", errorName);
	json.put("message", message);
	return json.toString();
    }
    
    public static Response getResultResponse(String json) {
	return Response.ok(json)
		.type(MediaType.APPLICATION_JSON).build();
    }
    
    public static Response getSuccessResponse(String message) {
	return Response.ok(getSuccessResponseString(message))
		.type(MediaType.APPLICATION_JSON).build();
    }
    
    public static Response getErrorResponse(Exception e, Response.Status status) {
	return Response.status(status)
		.entity(getErrorResponseString(status.getStatusCode(), e.getClass().getName(), e.getMessage()))
		.type(MediaType.APPLICATION_JSON).build();
    }
    
    public static Response getErrorResponse(Exception e, Response.Status status, String message) {
	return Response.status(status)
		.entity(getErrorResponseString(status.getStatusCode(), e.getClass().getName(), message))
		.type(MediaType.APPLICATION_JSON).build();
    }
}
