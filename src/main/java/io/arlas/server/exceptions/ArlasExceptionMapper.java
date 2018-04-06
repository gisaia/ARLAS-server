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

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import io.arlas.server.rest.ResponseFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ArlasExceptionMapper implements ExceptionMapper<ArlasException> {
    Logger logger = LoggerFactory.getLogger(ArlasExceptionMapper.class);

    @Override
    public Response toResponse(ArlasException e) {
        logger.error("Error occurred", e);
        if (e instanceof NotFoundException)
            return ResponseFormatter.getErrorResponse(e, Response.Status.NOT_FOUND, e.getMessage());
        else if (e instanceof InvalidParameterException)
            return ResponseFormatter.getErrorResponse(e, Response.Status.BAD_REQUEST, e.getMessage());
        else if (e instanceof NotAllowedException)
            return ResponseFormatter.getErrorResponse(e, Response.Status.BAD_REQUEST, e.getMessage());
        else if (e instanceof BadRequestException)
            return ResponseFormatter.getErrorResponse(e, Response.Status.BAD_REQUEST, e.getMessage());
        else if (e instanceof NotImplementedException)
            return ResponseFormatter.getErrorResponse(e, Response.Status.NOT_IMPLEMENTED, e.getMessage());
        else if (e instanceof OGCException){
            return ResponseFormatter.getWFSErrorResponse((OGCException)e);
        }
        else
            return ResponseFormatter.getErrorResponse(e, Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
