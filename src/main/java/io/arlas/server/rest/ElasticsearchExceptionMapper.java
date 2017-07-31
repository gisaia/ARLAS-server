package io.arlas.server.rest;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ElasticsearchExceptionMapper implements ExceptionMapper<ElasticsearchException> {
    Logger logger = LoggerFactory.getLogger(ArlasExceptionMapper.class);

    @Override
    public Response toResponse(ElasticsearchException e) {
        logger.error("Error occurred", e);
        if(e instanceof NoNodeAvailableException || e instanceof ClusterBlockException)
            return ResponseFormatter.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        else
            return ResponseFormatter.getErrorResponse(e, Response.Status.BAD_REQUEST, e.getMessage());
    }
}
