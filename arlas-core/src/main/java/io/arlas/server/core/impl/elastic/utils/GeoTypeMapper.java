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

package io.arlas.server.core.impl.elastic.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.NotImplementedException;
import io.arlas.commons.exceptions.ParseException;
import io.arlas.server.core.model.enumerations.GeoTypeEnum;
import io.arlas.server.core.utils.GeoUtil;
import io.arlas.server.core.utils.ParamsParser;
import jakarta.json.JsonObject;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeoTypeMapper {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectReader reader = mapper.readerFor(GeoJsonObject.class);

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoTypeMapper.class);
    public static final String NOT_SUPPORTED_GEO_FORMAT = "Not supported geo_point or geo_shape format found.";


    public static GeoJsonObject getGeoJsonObject(Object elasticsearchGeoField) throws ArlasException {
        return getGeoJsonObject(elasticsearchGeoField, getGeometryType(elasticsearchGeoField));
    }

    public static GeoJsonObject getGeoJsonObject(Object elasticsearchGeoField, GeoTypeEnum type) throws ArlasException {
        GeoJsonObject geoObject = null;
        String parseExceptionMsg = "Unable to parse " + elasticsearchGeoField.toString() + " as valid " + type;
        String loggerMsg = "Unable to parse " + elasticsearchGeoField + "as valid " + type + " from " + elasticsearchGeoField.getClass();
        switch (type) {
            case GEOPOINT_AS_STRING:
                try {
                    String[] geoPoint = elasticsearchGeoField.toString().split(",");
                    geoObject = new Point(Double.parseDouble(geoPoint[1]), Double.parseDouble(geoPoint[0]));
                } catch (Exception e) {
                    LOGGER.error(loggerMsg, e);
                    throw new ParseException(parseExceptionMsg);
                }
                break;
            case GEOHASH:
                try {
                    geoObject = GeoUtil.getGeohashCentre(elasticsearchGeoField.toString());
                } catch (Exception e) {
                    LOGGER.error(loggerMsg, e);
                    throw new ParseException(parseExceptionMsg);
                }
                break;
            case GEOPOINT_AS_ARRAY:
                try {
                    geoObject = new Point(((Number) ((ArrayList<?>) elasticsearchGeoField).get(0)).doubleValue(),
                            ((Number) ((ArrayList<?>) elasticsearchGeoField).get(1)).doubleValue());
                } catch (Exception e) {
                    LOGGER.error(loggerMsg, e);
                    throw new ParseException(parseExceptionMsg);
                }
                break;
            case GEOHASH_ARRAY:
                try {
                    List geohashes = (ArrayList) elasticsearchGeoField;
                    int middleIndex = (geohashes.size() / 2);
                    geoObject = GeoUtil.getGeohashCentre(geohashes.get(middleIndex).toString());
                } catch (Exception e) {
                    LOGGER.error(loggerMsg, e);
                    throw new ParseException(parseExceptionMsg);
                }
                break;
            case GEOPOINT:
                try {
                    geoObject = new Point(((Number) ((HashMap<?, ?>) elasticsearchGeoField).get("lon")).doubleValue(),
                            ((Number) ((HashMap<?, ?>) elasticsearchGeoField).get("lat")).doubleValue());
                } catch (Exception e) {
                    LOGGER.error(loggerMsg, e);
                    throw new ParseException(parseExceptionMsg);
                }
                break;
            case GEOJSON:
                //Standard GeoJSON object
                try {
                    if(elasticsearchGeoField instanceof JsonObject){
                        geoObject= reader.readValue(elasticsearchGeoField.toString());
                    }else{
                        geoObject = reader.readValue(mapper.writer().writeValueAsString(elasticsearchGeoField));
                    }
                } catch (IOException e) {
                    LOGGER.error(loggerMsg, e);
                    throw new ParseException(parseExceptionMsg);
                }
                break;
            case WKT:
                try {
                    GeoJsonWriter geoJsonWriter = new GeoJsonWriter();
                    Geometry g = GeoUtil.readWKT(elasticsearchGeoField.toString());
                    geoObject = reader.readValue(geoJsonWriter.write(g));
                } catch (IOException e) {
                    LOGGER.error(loggerMsg, e);
                    throw new ParseException(parseExceptionMsg);
                }
                break;
        }
        return geoObject;
    }

    public static GeoTypeEnum getGeometryType(Object geometry) throws ArlasException {
        if (geometry instanceof String) {
            if (isWkt(geometry.toString())) {
                return GeoTypeEnum.WKT;
            } else if (isStringGeoPoint(geometry.toString())) {
                return GeoTypeEnum.GEOPOINT_AS_STRING;
            } else if (isGeohash(geometry.toString())) {
                return GeoTypeEnum.GEOHASH;
            } else {
                LOGGER.error("Unknown geo_point or geo_shape format from " + geometry.getClass() + " :" + geometry);
                throw new NotImplementedException(NOT_SUPPORTED_GEO_FORMAT);
            }
        } else if (geometry instanceof ArrayList) {
            List geometries = (ArrayList) geometry;
            if (geometries.size() == 2
                    && ParamsParser.tryParseDouble(geometries.get(0).toString()) != null
                    && ParamsParser.tryParseDouble(geometries.get(1).toString()) != null) {
                return GeoTypeEnum.GEOPOINT_AS_ARRAY;
            } else {
                if (geometries.stream().filter(g -> isGeohash(g.toString())).count() == geometries.size()) {
                    return GeoTypeEnum.GEOHASH_ARRAY;
                } else {
                    LOGGER.error("Unknown geo_point or geo_shape format from " + geometry.getClass() + " :" + geometry);
                    throw new NotImplementedException(NOT_SUPPORTED_GEO_FORMAT);
                }
            }
        } else if (geometry instanceof HashMap) {
            if (((HashMap) geometry).containsKey("type")) {
                return GeoTypeEnum.GEOJSON;
            } else if (((HashMap) geometry).containsKey("lat") && ((HashMap) geometry).containsKey("lon")){
                return GeoTypeEnum.GEOPOINT;
            } else {
                LOGGER.error("Unknown geo_point or geo_shape format from " + geometry.getClass() + " :" + geometry);
                throw new NotImplementedException(NOT_SUPPORTED_GEO_FORMAT);
            }
        }   else {
            LOGGER.error("Unknown geo_point or geo_shape format from " + geometry.getClass() + " :" + geometry);
            throw new NotImplementedException(NOT_SUPPORTED_GEO_FORMAT);
        }
    }

    public static boolean isWkt(String geomString) {
        WKTReader wktReader = new WKTReader();
        try {
            wktReader.read(geomString);
            return true;
        } catch (Exception e){
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
}
