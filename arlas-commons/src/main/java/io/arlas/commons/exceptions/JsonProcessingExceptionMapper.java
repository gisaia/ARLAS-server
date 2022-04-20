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

package io.arlas.commons.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Arrays;

public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    Logger logger = LoggerFactory.getLogger(ArlasExceptionMapper.class);

    @Override
    public Response toResponse(JsonProcessingException e) {
        logger.error("Error occurred " + e.getClass().getName() + ": " + e.getMessage());
        for (StackTraceElement s : Arrays.copyOf(e.getStackTrace(), 10)) {
            logger.error("! " + s.toString());
        }
        return ArlasException.getResponse(e, Response.Status.BAD_REQUEST, "Malformed JSON parameter.");
    }
}
