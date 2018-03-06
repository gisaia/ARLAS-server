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

package io.arlas.server.wfs.utils;

import io.arlas.server.app.ArlasServerConfiguration;
import io.arlas.server.exceptions.ArlasConfigurationException;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;

public class VersionUtils {

    public static Version checkVersion(Version requestedVersion, ArlasServerConfiguration configuration)
            throws OWSException,ArlasConfigurationException {
        Logger LOGGER = LoggerFactory.getLogger(VersionUtils.class);

        Version version = requestedVersion;
        SortedSet<Version> offeredVersions = new TreeSet<Version>();
        List<String> supportedVersion = new ArrayList<>();
        supportedVersion.add("2.0.0");
        supportedVersion.forEach(v -> {
            try {
                offeredVersions.add(getVersion(v));
            } catch (OWSException e) {
                new OWSException( "INVALID VERSION", OWSException.INVALID_PARAMETER_VALUE, "version" );
            }
        });
        if ( requestedVersion == null ) {
            LOGGER.debug( "Assuming version 1.1.0 (the only one that has an optional version attribute)." );
            version = VERSION_110;
        }
        if ( !offeredVersions.contains( version ) ) {
            throw new OWSException( "INVALID VERSION", OWSException.INVALID_PARAMETER_VALUE, "version" );
        }
        return version;
    }


    public static Version getVersion( String versionString )
            throws OWSException {
        Version version = null;
        if ( versionString != null && !"".equals( versionString ) ) {
            try {
                version = Version.parseVersion( versionString );
            } catch ( InvalidParameterValueException e ) {
                throw new OWSException( e.getMessage(), OWSException.INVALID_PARAMETER_VALUE, "version" );
            }
        }
        return version;
    }
}
