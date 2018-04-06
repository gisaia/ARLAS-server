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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.NotImplementedException;
import org.elasticsearch.common.geo.GeoPoint;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GeoTypeMapper {

    private static ObjectMapper mapper = new ObjectMapper();
    private static ObjectReader reader = mapper.readerFor(GeoJsonObject.class);

    private static Logger LOGGER = LoggerFactory.getLogger(GeoTypeMapper.class);

    @SuppressWarnings("rawtypes")
    public static GeoJsonObject getGeoJsonObject(Object elasticsearchGeoField) throws ArlasException {
        GeoJsonObject geoObject = null;
        if (elasticsearchGeoField instanceof String) {
            //Standard lat/lon or geohash geo_point field
            try {
                GeoPoint geoPoint = new GeoPoint(elasticsearchGeoField.toString());
                geoObject = new Point(geoPoint.getLon(), geoPoint.getLat());
            } catch (Exception e) {
                LOGGER.error("unable to parse geo_point from " + elasticsearchGeoField.getClass() + " :" + elasticsearchGeoField, e);
                throw new NotImplementedException("Not supported geo_point format found.");
            }
        } else if (elasticsearchGeoField instanceof ArrayList
                && ((ArrayList) elasticsearchGeoField).size() == 2) {
            //Standard lon/lat array as geo_point field
            try {
                geoObject = new Point((Double) ((ArrayList) elasticsearchGeoField).get(0), (Double) ((ArrayList) elasticsearchGeoField).get(1));
            } catch (Exception e) {
                LOGGER.error("unable to parse geo_point from " + elasticsearchGeoField.getClass() + " :" + elasticsearchGeoField, e);
                throw new NotImplementedException("Not supported geo_point format found.");
            }
        } else if (elasticsearchGeoField instanceof HashMap) {
            if (((HashMap) elasticsearchGeoField).containsKey("type")) {
                //Standard GeoJSON object
                try {
                    geoObject = reader.readValue(mapper.writer().writeValueAsString(elasticsearchGeoField));
                } catch (IOException e) {
                    LOGGER.error("unable to parse geo_shape from " + elasticsearchGeoField.getClass() + " :" + elasticsearchGeoField, e);
                    throw new NotImplementedException("Not supported geo_point or geo_shape format found.");
                }
            } else if (((HashMap) elasticsearchGeoField).containsKey("lat") &&
                    ((HashMap) elasticsearchGeoField).containsKey("lon")) {
                try {
                    geoObject = new Point(((Double) ((HashMap) elasticsearchGeoField).get("lon")), ((Double) ((HashMap) elasticsearchGeoField).get("lat")));
                } catch (Exception e) {
                    LOGGER.error("unable to parse geo_point from " + elasticsearchGeoField.getClass() + " :" + elasticsearchGeoField, e);
                    throw new NotImplementedException("Not supported geo_point format found.");
                }
            } else {
                LOGGER.error("unknwon geo_point or geo_shape from " + elasticsearchGeoField.getClass() + " :" + elasticsearchGeoField);
                throw new NotImplementedException("Not supported geo_point or geo_shape format found.");
            }
        } else {
            LOGGER.error("unknwon geo_point or geo_shape from " + elasticsearchGeoField.getClass() + " :" + elasticsearchGeoField);
            throw new NotImplementedException("Not supported geo_point or geo_shape format found.");
        }
        return geoObject;
    }

    public static void main(String[] args) {

    }

}
