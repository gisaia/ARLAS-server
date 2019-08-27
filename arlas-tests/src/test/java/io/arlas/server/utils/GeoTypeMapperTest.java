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

package io.arlas.server.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.enumerations.GeoTypeEnum;
import org.geojson.Point;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

public class GeoTypeMapperTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetGeoJsonObject() throws ArlasException, JsonProcessingException {
        String pointStringLatLon = "41.12,-71.34";
        String pointStringGeohash = "drm3btev3e86";
        String pointStringWKT = "POINT(-71.34 41.12)";
        ArrayList pointArray = new ArrayList();
        pointArray.add(-71.34);
        pointArray.add(41.12);
        HashMap pointMap = new HashMap();
        pointMap.put("lat", 41.12);
        pointMap.put("lon", -71.34);

        HashMap pointGeojson = new HashMap();
        pointGeojson.put("type", "Point");
        pointGeojson.put("coordinates", pointArray);

        assertTrue(GeoTypeMapper.getGeometryType(pointStringLatLon).equals(GeoTypeEnum.GEOPOINT_AS_STRING));
        assertTrue(GeoTypeMapper.getGeometryType(pointStringGeohash).equals(GeoTypeEnum.GEOHASH));
        assertTrue(GeoTypeMapper.getGeometryType(pointStringWKT).equals(GeoTypeEnum.WKT));
        assertTrue(GeoTypeMapper.getGeometryType(pointArray).equals(GeoTypeEnum.GEOPOINT_AS_ARRAY));
        assertTrue(GeoTypeMapper.getGeometryType(pointMap).equals(GeoTypeEnum.GEOPOINT));
        assertTrue(GeoTypeMapper.getGeometryType(pointGeojson).equals(GeoTypeEnum.GEOJSON));

        Point refPoint = new Point(-71.34, 41.12);
        assertTrue(GeoTypeMapper.getGeoJsonObject(pointStringLatLon).equals(refPoint));
        assertTrue(GeoTypeMapper.getGeoJsonObject(pointStringGeohash) instanceof Point);
        assertTrue(GeoTypeMapper.getGeoJsonObject(pointStringWKT) instanceof Point);
        assertTrue(((Point)GeoTypeMapper.getGeoJsonObject(pointStringWKT)).getCoordinates().equals(refPoint.getCoordinates()));
        assertTrue(GeoTypeMapper.getGeoJsonObject(pointArray).equals(refPoint));
        assertTrue(GeoTypeMapper.getGeoJsonObject(pointMap).equals(refPoint));
        assertTrue(GeoTypeMapper.getGeoJsonObject(pointGeojson).equals(refPoint));
    }

}
