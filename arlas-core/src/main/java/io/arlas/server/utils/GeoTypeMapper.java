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
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.exceptions.NotImplementedException;
import io.arlas.server.model.enumerations.GeoTypeEnum;
import org.elasticsearch.common.geo.GeoPoint;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GeoTypeMapper {

    private static ObjectMapper mapper = new ObjectMapper();
    private static ObjectReader reader = mapper.readerFor(GeoJsonObject.class);

    private static Logger LOGGER = LoggerFactory.getLogger(GeoTypeMapper.class);
    public static final String INVALID_WKT_RANGE = "Invalid WKT geometry. Coordinates out of range";
    public static final String INVALID_WKT = "Invalid WKT geometry.";


    @SuppressWarnings("rawtypes")
    public static GeoJsonObject getGeoJsonObject(Object elasticsearchGeoField) throws ArlasException {
        return getGeoJsonObject(elasticsearchGeoField, getGeometryType(elasticsearchGeoField));
    }

    public static GeoJsonObject getGeoJsonObject(Object elasticsearchGeoField, GeoTypeEnum type) throws ArlasException {
        GeoJsonObject geoObject = null;
        switch (type) {
            case GEOPOINT_AS_STRING:
            case GEOHASH:
                try {
                    GeoPoint geoPoint = new GeoPoint(elasticsearchGeoField.toString());
                    geoObject = new Point(geoPoint.getLon(), geoPoint.getLat());
                } catch (Exception e) {
                    LOGGER.error("unable to parse geo_point from " + elasticsearchGeoField.getClass() + " :" + elasticsearchGeoField, e);
                    throw new NotImplementedException("Not supported geo_point format found.");
                }
                break;
            case GEOPOINT_AS_ARRAY:
                try {
                    geoObject = new Point(((Number) ((ArrayList) elasticsearchGeoField).get(0)).doubleValue(), ((Number) ((ArrayList) elasticsearchGeoField).get(1)).doubleValue());
                } catch (Exception e) {
                    LOGGER.error("unable to parse geo_point from " + elasticsearchGeoField.getClass() + " :" + elasticsearchGeoField, e);
                    throw new NotImplementedException("Not supported geo_point format found.");
                }
                break;
            case GEOPOINT:
                try {
                    geoObject = new Point(((Number) ((HashMap) elasticsearchGeoField).get("lon")).doubleValue(), ((Number) ((HashMap) elasticsearchGeoField).get("lat")).doubleValue());
                } catch (Exception e) {
                    LOGGER.error("unable to parse geo_point from " + elasticsearchGeoField.getClass() + " :" + elasticsearchGeoField, e);
                    throw new NotImplementedException("Not supported geo_point format found.");
                }
                break;
            case GEOJSON:
                //Standard GeoJSON object
                try {
                    geoObject = reader.readValue(mapper.writer().writeValueAsString(elasticsearchGeoField));
                } catch (IOException e) {
                    LOGGER.error("unable to parse geo_point or geo_shape from " + elasticsearchGeoField.getClass() + " :" + elasticsearchGeoField, e);
                    throw new NotImplementedException("Not supported geo_point or geo_shape format found.");
                }
                break;
            case WKT:
                try {
                    GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
                    Geometry g = readWKT(elasticsearchGeoField.toString());
                    geoObject = reader.readValue(geoJsonWriter.write(g));
                } catch (IOException e) {
                    LOGGER.error("unable to parse geo_point or geo_shape from " + elasticsearchGeoField.getClass() + " :" + elasticsearchGeoField, e);
                    throw new NotImplementedException("Not supported geo_point or geo_shape format found.");
                }
                break;
            case UNKNOWN:
                LOGGER.error("unknwon geo_point or geo_shape from " + elasticsearchGeoField.getClass() + " :" + elasticsearchGeoField);
                throw new NotImplementedException("Not supported geo_point or geo_shape format found.");
        }
        return geoObject;
    }

    public static GeoTypeEnum getGeometryType(Object geometry) {
        if (geometry instanceof String) {
            if (isWkt(geometry.toString())) {
                return GeoTypeEnum.WKT;
            } else if (isStringGeoPoint(geometry.toString())) {
                return GeoTypeEnum.GEOPOINT_AS_STRING;
            } else if (isGeohash(geometry.toString())) {
                return GeoTypeEnum.GEOHASH;
            } else return GeoTypeEnum.UNKNOWN;
        } else if (geometry instanceof ArrayList  && ((ArrayList) geometry).size() == 2) {
            return GeoTypeEnum.GEOPOINT_AS_ARRAY;
        } else if (geometry instanceof HashMap) {
            if (((HashMap) geometry).containsKey("type")) {
                return GeoTypeEnum.GEOJSON;
            } else if (((HashMap) geometry).containsKey("lat") && ((HashMap) geometry).containsKey("lon")){
                return GeoTypeEnum.GEOPOINT;
            } else {
                return GeoTypeEnum.UNKNOWN;
            }
        } else {
            return GeoTypeEnum.UNKNOWN;
        }
    }

    public static boolean isWkt(String geomString) {
        WKTReader wktReader = new WKTReader();
        try {
            wktReader.read(geomString);
            return true;
        } catch (ParseException e){
            return false;
        }
    }

    public static boolean isStringGeoPoint(String geomString) {
        Pattern stringGeoPointPattern = Pattern.compile("^-?(\\d*\\.)?\\d+,-?(\\d*\\.)?\\d+$");
        Matcher m = stringGeoPointPattern.matcher(geomString);
        return m.matches();
    }

    public static boolean isGeohash(String geomString) {
        Pattern geohashPattern = Pattern.compile("^[a-z0-9]+$");
        Matcher m = geohashPattern.matcher(geomString);
        return m.matches();
    }

    public static Geometry readWKT(String geometry) throws ArlasException {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Envelope affectedBounds = new Envelope(-360, 360, -180, 180);
        WKTReader wkt = new WKTReader(geometryFactory);
        Geometry geom = null;
        try {
            geom = wkt.read(geometry);
            List<Coordinate> filteredCoord = Arrays.stream(geom.getCoordinates()).filter(coordinate -> affectedBounds.contains(coordinate)).collect(Collectors.toList());
            if(filteredCoord.size() != geom.getCoordinates().length){
                throw new InvalidParameterException(INVALID_WKT_RANGE);
            }
            IsValidOp vaildOp = new IsValidOp(geom);
            TopologyValidationError err = vaildOp.getValidationError();
            if (err != null)
            {
                throw new InvalidParameterException(INVALID_WKT);
            }
        } catch (ParseException ex) {
            throw new InvalidParameterException(INVALID_WKT);
        }
        return geom;
    }
}
