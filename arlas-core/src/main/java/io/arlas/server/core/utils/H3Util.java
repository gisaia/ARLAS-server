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

package io.arlas.server.core.utils;

import com.uber.h3core.H3Core;
import com.uber.h3core.util.GeoCoord;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class H3Util {
    private static Logger LOGGER = LoggerFactory.getLogger(H3Util.class);
    private H3Core h3Core;

    private final static H3Util h3Util = new H3Util();

    public static H3Util getInstance() {
        return h3Util;
    }

    private H3Util() {
        try {
            h3Core = H3Core.newInstance();
        } catch (IOException e) {
            LOGGER.error("!!! Could not instanciate H3Core. H3 will not be supported in this server instance !!!", e);
        }
    }

    public Pair<Double, Double> getCellCenterAsLatLon(String h3AsHex) {
        GeoCoord gc = h3Core.h3ToGeo(h3AsHex);
        return Pair.of(gc.lat, gc.lng);
    }

    public List<Pair<Double, Double>> getCellBoundaryAsLatLonList(String h3AsHex) {
        List<GeoCoord> gc = h3Core.h3ToGeoBoundary(h3AsHex);
        return gc.stream().map(g -> Pair.of(g.lat, g.lng)).collect(Collectors.toList());
    }

}
