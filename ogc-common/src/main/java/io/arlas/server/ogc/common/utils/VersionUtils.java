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

import io.arlas.commons.utils.StringUtil;
import io.arlas.server.ogc.common.exceptions.OGC.OGCException;
import io.arlas.server.ogc.common.exceptions.OGC.OGCExceptionCode;
import io.arlas.server.ogc.common.model.Service;

import java.util.SortedSet;
import java.util.TreeSet;

public class VersionUtils {

    public static Version checkVersion(Version requestedVersion, String supportedVersion, Service service) throws OGCException {
        Version version = requestedVersion;
        SortedSet<Version> offeredVersions = new TreeSet<>();
        try {
            offeredVersions.add(getVersion(supportedVersion, service));
        } catch (OGCException e) {
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, "INVALID VERSION", "version", service);
        }
        if (!offeredVersions.contains(version)) {
            throw new OGCException(OGCExceptionCode.VERSION_NEGOTIATION_FAILED, "INVALID acceptVersions", "acceptVersions", service);
        }
        return version;
    }

    public static Version getVersion(String versionString, Service service) throws OGCException {
        Version version = null;
        if (!StringUtil.isNullOrEmpty(versionString)) {
            try {
                version = Version.parseVersion(versionString, service);
            } catch (OGCException e) {
                throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, e.getMessage(), "version", service);
            }
        }
        return version;
    }
}
