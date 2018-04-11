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
import io.arlas.server.ogc.common.utils.Version;
import io.arlas.server.ogc.common.utils.VersionUtils;

import java.util.Arrays;
import java.util.List;

public class CSWCheckParam {

    public static void checkQuerySyntax(String elementName, String elementSetName, String acceptVersions, String version, String service) throws OGCException {
        if(service==null){
            throw new OGCException(OGCExceptionCode.MISSING_PARAMETER_VALUE, "Missing service", "service", Service.CSW);

        }
        ElementSetName elementSetNameEnum ;
        if(elementSetName!=null){
            try{
                elementSetNameEnum= ElementSetName.valueOf(elementSetName);
            }catch (IllegalArgumentException e){
                String msg = "Invalid elementSetName value, allowed values : " +
                        ElementSetName.brief.name() + ", " + ElementSetName.summary.name()   + "or " + ElementSetName.full.name();
                throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid elementSetName", "elementSetName", Service.CSW);
            }
        }
        if (elementName != null && elementSetName != null) {
            throw new OGCException(OGCExceptionCode.NO_APPLICABLE_CODE_400, "elementName and elementSetName can't be used together", "elementName,elementSetName", Service.CSW);
        }
        Version requestVersion = null;
        if (acceptVersions != null) {
            List<String> versions = Arrays.asList(acceptVersions.split(","));
            for (String v : versions) {
                requestVersion = VersionUtils.getVersion(v, Service.CSW);
                VersionUtils.checkVersion(requestVersion, CSWConstant.SUPPORTED_CSW_VERSION, Service.CSW);
            }
        } else {
            requestVersion = VersionUtils.getVersion(version, Service.CSW);
            VersionUtils.checkVersion(requestVersion, CSWConstant.SUPPORTED_CSW_VERSION, Service.CSW);
        }
    }
}
