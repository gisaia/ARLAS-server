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

package io.arlas.server.ogc.csw.utils;

import io.arlas.server.exceptions.INSPIRE.INSPIREException;
import io.arlas.server.exceptions.INSPIRE.INSPIREExceptionCode;
import io.arlas.server.inspire.common.constants.INSPIREConstants;
import io.arlas.server.ogc.common.model.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CSWParamsParser {

    public static Date getMetadataDate(String value) throws INSPIREException {
        try {
            return new SimpleDateFormat(INSPIREConstants.DUBLIN_CORE_DATE_FORMAT).parse(value);
        } catch (ParseException e) {
            throw new INSPIREException(INSPIREExceptionCode.INTERNAL_SERVER_ERROR, "Metadata date of collection is not well formatted", Service.CSW);
        }
    }
}
