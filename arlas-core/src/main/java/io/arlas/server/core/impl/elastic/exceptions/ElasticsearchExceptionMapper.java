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

package io.arlas.server.core.impl.elastic.exceptions;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.ArlasExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ElasticsearchExceptionMapper implements ExceptionMapper<ElasticsearchException> {
    Logger logger = LoggerFactory.getLogger(ArlasExceptionMapper.class);

    @Override
    public Response toResponse(ElasticsearchException e) {
        logger.error("Error occurred", e);
        logger.error("e.endpointId()=" + e.endpointId());
        logger.error("e.error()=" + e.error());
        logger.error("e.response()" + e.response());
        logger.error("e.status()" + e.status());
        return ArlasException.getResponse(e, Response.Status.BAD_REQUEST, e.getMessage());
    }
}
