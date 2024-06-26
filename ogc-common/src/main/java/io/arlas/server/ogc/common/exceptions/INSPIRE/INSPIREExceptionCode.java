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

package io.arlas.server.ogc.common.exceptions.INSPIRE;

import io.arlas.server.ogc.common.exceptions.OGC.OGCExceptionCode;
import jakarta.servlet.http.HttpServletResponse;

public class INSPIREExceptionCode extends OGCExceptionCode {
    public static final INSPIREExceptionCode MISSING_INSPIRE_METADATA = new INSPIREExceptionCode("InspireMissingMetadata", HttpServletResponse.SC_BAD_REQUEST);
    public static final INSPIREExceptionCode INVALID_INSPIRE_PARAMETER_VALUE = new INSPIREExceptionCode("InspireInvalidParameterValue", HttpServletResponse.SC_BAD_REQUEST);

    public INSPIREExceptionCode(String value, Integer httpStatusCode) {
        super(value, httpStatusCode);
    }
}
