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

import io.arlas.server.exceptions.OGCException;
import io.arlas.server.exceptions.OGCExceptionCode;
import io.arlas.server.ogc.common.model.Service;

public class CSWCheckParam {

    public static void checkQuerySyntax(String elementName, String elementSetName) throws OGCException {
        if (elementName != null && elementSetName != null) {
            throw new OGCException(OGCExceptionCode.NO_APPLICABLE_CODE_400, "elementName and elementSetName can't be used together", "elementName,elementSetName", Service.CSW);
        }
    }
}