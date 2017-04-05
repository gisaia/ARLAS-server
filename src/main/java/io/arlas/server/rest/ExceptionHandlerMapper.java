package io.arlas.server.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.omg.CORBA.portable.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Provider
public class ExceptionHandlerMapper implements ExceptionMapper<ApplicationException> {
	Logger logger = LoggerFactory.getLogger(ExceptionHandlerMapper.class);

	@Override
	public Response toResponse(ApplicationException e) {
		logger.error("Error occurred", e);
		return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();  
	}
}
