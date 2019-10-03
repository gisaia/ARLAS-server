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

package io.arlas.server.ogc.wfs.utils;


import io.arlas.server.ogc.common.exceptions.OGC.OGCException;
import io.arlas.server.ogc.common.exceptions.OGC.OGCExceptionCode;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.common.utils.Version;
import io.arlas.server.ogc.common.utils.VersionUtils;

import java.util.Arrays;

public class WFSCheckParam {

    public static void checkQuerySyntax(String service, String bbox, String resourceid, String filter, WFSRequestType requestType, Version requestVersion) throws OGCException {

        if (bbox != null && resourceid != null) {
            throw new OGCException(OGCExceptionCode.OPERATION_NOT_SUPPORTED, "BBOX and RESOURCEID can't be used together", "bbox,resourceid", Service.WFS);
        } else if (bbox != null && filter != null) {
            throw new OGCException(OGCExceptionCode.OPERATION_NOT_SUPPORTED, "BBOX and FILTER can't be used together", "bbox,filter", Service.WFS);
        } else if (resourceid != null && filter != null) {
            throw new OGCException(OGCExceptionCode.OPERATION_NOT_SUPPORTED, "RESOURCEID and FILTER can't be used together", "bbox,filter", Service.WFS);
        }

        if (requestType != WFSRequestType.GetCapabilities) {
            if (requestVersion == null) {
                String msg = "Missing version parameter.";
                throw new OGCException(OGCExceptionCode.MISSING_PARAMETER_VALUE, msg, "version", Service.WFS);
            }
            VersionUtils.checkVersion(requestVersion, WFSConstant.SUPPORTED_WFS_VERSION, Service.WFS);
        } else {
            if (service == null || !service.equals("WFS")) {
                String msg = "Missing service parameter.";
                throw new OGCException(OGCExceptionCode.MISSING_PARAMETER_VALUE, msg, "service", Service.WFS);
            }
        }
    }

    public static void checkTypeNames(String collectionName, String typenames) throws OGCException {
        if (typenames != null) {
            if (!typenames.contains(collectionName)) {
                throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "FeatureType " + typenames + " not exist", "typenames", Service.WFS);
            }
        }
    }

    public static void checkSrsName(String srsname) throws OGCException {
        boolean isCrsUnSupported = false;
        if (srsname != null) {
            if (Arrays.asList(WFSConstant.SUPPORTED_CRS).indexOf(srsname) < 0) {
                isCrsUnSupported = true;
            }
        } else {
            isCrsUnSupported = false;
        }
        if (isCrsUnSupported) {
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "Invalid CRS :" + srsname, "srsname", Service.WFS);
        }
    }
}
