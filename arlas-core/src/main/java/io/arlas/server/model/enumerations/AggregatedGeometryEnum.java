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

package io.arlas.server.model.enumerations;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum AggregatedGeometryEnum {
    BBOX("bbox"), CENTROID("centroid"), TILE("tile"), TILECENTER("tile_center");

    private final String value;
    private static final String INVALID_AGGREGATED_GEOMETRY = "Invalid aggregated geometry `";

    AggregatedGeometryEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
    public static AggregatedGeometryEnum fromValue(String v) throws ArlasException {
        for (AggregatedGeometryEnum c: AggregatedGeometryEnum.values()) {
            if (c.value.equals(v.toLowerCase()) || c.name().toLowerCase().equals(v.toLowerCase())) {
                return c;
            }
        }
        throw new InvalidParameterException(INVALID_AGGREGATED_GEOMETRY + v + "`. Must be one of " + Arrays.stream(AggregatedGeometryEnum.values()).map(c -> c.value()).collect(Collectors.joining(",")));
    }
}
