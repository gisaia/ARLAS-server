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

import javax.servlet.http.HttpServletResponse;

public class WFSExceptionCode {
    private final String value;
    private final Integer httpStatusCode;

    // OWS 2.0 exception codes
    public static final WFSExceptionCode OPERATION_NOT_SUPPORTED = new WFSExceptionCode("OperationNotSupported", HttpServletResponse.SC_NOT_IMPLEMENTED);
    public static final WFSExceptionCode MISSING_PARAMETER_VALUE = new WFSExceptionCode("MissingParameterValue", HttpServletResponse.SC_BAD_REQUEST);
    public static final WFSExceptionCode INVALID_PARAMETER_VALUE = new WFSExceptionCode("InvalidParameterValue", HttpServletResponse.SC_BAD_REQUEST);
    public static final WFSExceptionCode NO_APPLICABLE_CODE = new WFSExceptionCode("NoApplicableCode", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    // WFS 2.0 exception codes
    public static final WFSExceptionCode OPERATION_PROCESSING_FAILED = new WFSExceptionCode("OperationProcessingFailed", HttpServletResponse.SC_BAD_REQUEST);
    public static final WFSExceptionCode NOT_FOUND = new WFSExceptionCode("NotFound", HttpServletResponse.SC_NOT_FOUND);
    // Server exception codes
    public static final WFSExceptionCode INTERNAL_SERVER_ERROR = new WFSExceptionCode("InternalServerError", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    public WFSExceptionCode(String value, Integer httpStatusCode) {
        this.value = value;
        this.httpStatusCode = httpStatusCode;
    }

    public WFSExceptionCode(String value) {
        this(value, null);
    }

    public String getValue() {
        return value;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

}
