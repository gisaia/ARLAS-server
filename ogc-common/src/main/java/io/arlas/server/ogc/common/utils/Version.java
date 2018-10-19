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

public class Version implements Comparable<Version> {

    private int x;

    private int y;

    private int z;

    /**
     * Constructs a <code>Version</code> for an OWS operation.
     *
     * @param x major version. Must be a positive integer.
     * @param y minor version. Must be between 0 and 99.
     * @param z minor sub version. Must be between 0 and 99.
     * @throws OGCException if a parameters exceed the allowed range
     */
    public Version(int x, int y, int z, Service service) throws OGCException {

        if (x < 0 || y < 0 || z < 0 || y > 99 || z > 99) {
            String msg = x + "." + y + "." + z + " is not a valid OGC/OWS version value.";
            throw new OGCException(msg, service);
        }

        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Parses the string argument as a <code>Version</code>.
     * <p>
     * The string value shall contain one x.y.z "version" value (e.g., "2.1.3"). A version number shall contain three
     * non-negative integers separated by decimal points, in the form "x.y.z". The integers y and z shall not exceed 99.
     *
     * @param s a <code>String</code> containing the <code>Version</code> representation to be parsed
     * @return a corresponding <code>Version</code> object
     * @throws OGCException if the string does not contain a parsable <code>Version</code>
     */

    public static Version parseVersion(String s, Service service)
            throws OGCException {
        String[] parts = s.split("\\.");
        if (parts.length != 3) {
            String msg = "String '" + s + " is not a valid OGC/OWS version value.";
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, msg, "version", service);
        }

        int x = -1;
        int y = -1;
        int z = -1;

        try {
            x = Integer.parseInt(parts[0]);
            y = Integer.parseInt(parts[1]);
            z = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            String msg = "String '" + s + " is not a valid OGC/OWS version value.";
            throw new OGCException(OGCExceptionCode.INVALID_PARAMETER_VALUE, msg, "version", service);
        }
        return new Version(x, y, z, service);
    }

    public int compareTo(Version version) {
        if (this.x > version.x) {
            return 1;
        } else if (this.x < version.x) {
            return -1;
        }
        if (this.y > version.y) {
            return 1;
        } else if (this.y < version.y) {
            return -1;
        }
        if (this.z > version.z) {
            return 1;
        } else if (this.z < version.z) {
            return -1;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        // note: 7, 11 and 13 are prime numbers
        int hash = 7 * (x + 1);
        hash *= 11 * (y + 1);
        hash *= 13 * (z + 1);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Version)) {
            return false;
        }
        Version that = (Version) obj;
        return this.x == that.x && this.y == that.y && this.z == that.z;
    }

    @Override
    public String toString() {
        return x + "." + y + "." + z;
    }

    /**
     * Returns a formatted string for presenting a series of versions to a human.
     *
     * @param versions versions to be listed
     * @return formatted, human-readable string
     */
    public static String getVersionsString(Version... versions) {
        int i = 0;
        StringBuilder s = new StringBuilder();
        for (Version version : versions) {
            s.append("'").append(version).append("'");
            if (i++ != versions.length - 1) {
                s.append(", ");
            }
        }
        return s.toString();
    }
}