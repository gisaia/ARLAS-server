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

package io.arlas.server.ogc.common.exceptions.OGC;

import jakarta.servlet.http.HttpServletResponse;

public class OGCExceptionCode {
    private final String value;
    private final Integer httpStatusCode;

    // OWS 2.0 exception codes
    public static final OGCExceptionCode OPERATION_NOT_SUPPORTED = new OGCExceptionCode("OperationNotSupported", HttpServletResponse.SC_NOT_IMPLEMENTED);
    public static final OGCExceptionCode MISSING_PARAMETER_VALUE = new OGCExceptionCode("MissingParameterValue", HttpServletResponse.SC_BAD_REQUEST);
    public static final OGCExceptionCode INVALID_PARAMETER_VALUE = new OGCExceptionCode("InvalidParameterValue", HttpServletResponse.SC_BAD_REQUEST);
    public static final OGCExceptionCode NO_APPLICABLE_CODE = new OGCExceptionCode("NoApplicableCode", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    public static final OGCExceptionCode NO_APPLICABLE_CODE_400 = new OGCExceptionCode("NoApplicableCode", HttpServletResponse.SC_BAD_REQUEST);
    public static final OGCExceptionCode VERSION_NEGOTIATION_FAILED = new OGCExceptionCode("VersionNegotiationFailed", HttpServletResponse.SC_BAD_REQUEST);

    // WFS 2.0 exception codes
    public static final OGCExceptionCode OPERATION_PROCESSING_FAILED = new OGCExceptionCode("OperationProcessingFailed", HttpServletResponse.SC_BAD_REQUEST);
    public static final OGCExceptionCode NOT_FOUND = new OGCExceptionCode("NotFound", HttpServletResponse.SC_NOT_FOUND);
    // FES 2.0 exception codes
    public static final OGCExceptionCode MISSING_ATTRIBUTE_FOR_OPERATOR = new OGCExceptionCode("MissingAttributeForOperator", HttpServletResponse.SC_NOT_FOUND);


    // Server exception codes
    public static final OGCExceptionCode INTERNAL_SERVER_ERROR = new OGCExceptionCode("InternalServerError", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    public OGCExceptionCode(String value, Integer httpStatusCode) {
        this.value = value;
        this.httpStatusCode = httpStatusCode;
    }

    public OGCExceptionCode(String value) {
        this(value, null);
    }

    public String getValue() {
        return value;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

}
