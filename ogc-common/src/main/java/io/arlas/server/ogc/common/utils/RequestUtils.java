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

package io.arlas.server.ogc.common.utils;

import io.arlas.server.exceptions.OGC.OGCException;
import io.arlas.server.exceptions.OGC.OGCExceptionCode;
import io.arlas.server.ogc.common.model.Service;

import java.util.Arrays;

public class RequestUtils {

    public static void checkRequestTypeByName(String requestName, String[] requestTypes, Service service) throws OGCException {
        String msg = "Request type '" + requestName + "' is not supported.";
        if (Arrays.asList(requestTypes).indexOf(requestName) < 0) {
            switch (service){
                case CSW:
                    throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, msg, "request", service);
                default:
                    throw new OGCException(OGCExceptionCode.OPERATION_NOT_SUPPORTED, msg, "request", service);
            }
        }
    }
}
