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

package io.arlas.server.core.exceptions;

import io.arlas.server.core.model.response.Error;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ArlasException extends Exception {
    private static final long serialVersionUID = 1L;

    protected Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;

    public ArlasException() {
    }

    public ArlasException(String message) {
        super(message);
    }

    public ArlasException(String message, Throwable cause) {
        super(message, cause);
    }

    public Response getResponse() {
        return Response.status(status).entity(new Error(status.getStatusCode(), this.getClass().getName(), this.getMessage()))
                .type(MediaType.APPLICATION_JSON).build();
    }

    public static Response getResponse(Exception e, Response.Status status, String message) {
        return Response.status(status).entity(new Error(status.getStatusCode(), e.getClass().getName(), message))
                .type(MediaType.APPLICATION_JSON).build();
    }
}
