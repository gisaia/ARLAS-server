/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.server.exceptions;

import io.arlas.server.rest.ResponseFormatter;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
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
        if (e instanceof NoNodeAvailableException || e instanceof ClusterBlockException)
            return ResponseFormatter.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        else if (e instanceof SearchPhaseExecutionException)
            return ResponseFormatter.getErrorResponse(e, Response.Status.BAD_REQUEST, e.getCause().getMessage());
        else
            return ResponseFormatter.getErrorResponse(e, Response.Status.BAD_REQUEST, e.getMessage());
    }
}
