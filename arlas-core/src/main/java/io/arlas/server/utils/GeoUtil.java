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

import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GeoUtil {

    public static final String INVALID_WKT_RANGE = "Invalid WKT geometry. Coordinates must be contained in the Envelope -360, 360, -180, 180";
    public static final String INVALID_WKT = "Invalid WKT geometry.";
    /**
     *
     * @param bbox 'west,south,east,north'
     * @return a clock-wise WKT Polygon/MultiPolygon as a String
     * @throws ArlasException
     */
    public static String bboxToWKT(String bbox) throws ArlasException {
        double[] coords = CheckParams.toDoubles(bbox);
        double west = coords[0];
        double south = coords[1];
        double east = coords[2];
        double north = coords[3];
        if (west > east) {
            return "MULTIPOLYGON(((" + -180 + " " + south + "," +
                    -180 + " " + north + "," +
                    east + " " + north + "," +
                    east + " " + south + "," +
                    -180 + " " + south +
                    "))," +
                    "((" + 180 +  " " + south + "," +
                    180 + " " + north + "," +
                    west + " " + north + "," +
                    west + " " + south + "," +
                    180 + " " + south +
                    ")))";
        }
        return "POLYGON((" + west + " " + south + "," +
                west + " " + north + "," +
                east + " " + north + "," +
                east + " " + south + "," +
                west + " " + south + "))";
    }

    /**
     *
     * @param g Geometry object
     * @return returns a WKT Geometry were longitudes are between -180 and 180
     */
    public static Geometry toCanonicalLongitudes(Geometry g) throws ArlasException{
        CoordinateFilter coordinateFilter = new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                if (coordinate.x >= 180) {
                    coordinate.x = coordinate.x - 360;
                } else if (coordinate.x <= -180) {
                    coordinate.x = coordinate.x + 360;
                }
            }
        };
        g.apply(coordinateFilter);
        return g;
    }

    /**
     * This method change translates longitudes by `offset` beyond (positive = true) or before (positive = false) `limitLon`
     * @param g geometry
     * @param offset translation offset
     * @param positive boolean that defines the translation sens
     * @param limitLon Min (positive=true) or max (positive=false) longitude of the passed geometry
     */
    public static void translateLongitudesWithCondition(Geometry g, double offset, boolean positive, double limitLon) {
        CoordinateFilter coordinateFilter = new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                if (positive) {
                    coordinate.x = coordinate.x < limitLon ? coordinate.x + offset:coordinate.x;
                } else {
                    coordinate.x = coordinate.x > limitLon ? coordinate.x - offset:coordinate.x;
                }
            }
        };
        g.apply(coordinateFilter);
    }

    public static void translateLongitudes(Geometry g, double offset, boolean positive) {
        CoordinateFilter coordinateFilter = new CoordinateFilter() {
            @Override
            public void filter(Coordinate coordinate) {
                if (positive) {
                    coordinate.x = coordinate.x + offset;
                } else {
                    coordinate.x = coordinate.x -offset;
                }
            }
        };
        g.apply(coordinateFilter);
    }
    /**
     *
     * @param geometry WKT string
     * @return a WKT geometry object
     * @throws ArlasException
     */
    public static Geometry readWKT(String geometry) throws ArlasException {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        WKTReader wkt = new WKTReader(geometryFactory);
        Geometry geom = null;
        try {
            // Geometry validity is already checked when parsing requests parameters
            geom = wkt.read(geometry);
        } catch (ParseException ex) {
            throw new InvalidParameterException(INVALID_WKT + ": " + ex.getMessage());
        }
        return geom;
    }

    public static void checkWKT(String wktString) throws InvalidParameterException {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Envelope affectedBounds = new Envelope(-360, 360, -180, 180);
        WKTReader wkt = new WKTReader(geometryFactory);
        Geometry geom = null;
        try {
            geom = wkt.read(wktString);
            List<Coordinate> filteredCoord = Arrays.stream(geom.getCoordinates()).filter(coordinate -> affectedBounds.contains(coordinate)).collect(Collectors.toList());
            if(filteredCoord.size() != geom.getCoordinates().length){
                throw new InvalidParameterException(INVALID_WKT_RANGE);
            }
            for(int i = 0; i< geom.getNumGeometries(); i++) {
                IsValidOp validOp = new IsValidOp(geom.getGeometryN(i));
                TopologyValidationError err = validOp.getValidationError();
                if (err != null) {
                    throw new InvalidParameterException(INVALID_WKT + ": " + err.getMessage());
                }
            }

        } catch (ParseException ex) {
            throw new InvalidParameterException(INVALID_WKT + ": " + ex.getMessage());
        }
    }

    /**
     *
     * @param geometry CW oriented geometry
     * @return List of geometries with longitudes between -180 and 180 and a join of thoses geometries in a MultiPolygon
     * @throws ArlasException
     */
    public static Tuple2<List<Geometry>, Geometry> splitGeometryOnDateline(Geometry geometry) throws ArlasException {
        Envelope envelope = geometry.getEnvelopeInternal();
        List<Geometry> geometries = new ArrayList<>();
        double envelopeEast = envelope.getMaxX();
        double envelopeWest = envelope.getMinX();
        if (envelopeEast <= 180 && envelopeWest >= -180) {
            /** longitudes between -180 and 180**/
            geometries.add(geometry);
            return new Tuple2(geometries, geometry);
        } else {
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            Geometry middle = geometryFactory.toGeometry(new Envelope(-180, 180, -90, 90));
            Geometry left = geometryFactory.toGeometry(new Envelope(-360, -180, -90, 90));
            Geometry right = geometryFactory.toGeometry(new Envelope(180, 360, -90, 90));
            if (envelopeWest >= 180 || envelopeEast <= -180) {
                /** longitudes between 180 and 360 OR longitudes between -360 and -180**/
                geometries.add(toCanonicalLongitudes(geometry));
                return new Tuple2(geometries, geometry);
            } else if (envelopeEast > 180) {
                /**  west is between -180 and 180 & east is beyond 180*/
                Polygon[] polygons = {(Polygon)middle.intersection(geometry), (Polygon)toCanonicalLongitudes(right.intersection(geometry))};
                geometries.add(polygons[0]);
                geometries.add(polygons[1]);
                return new Tuple2<>(geometries, new MultiPolygon(polygons,geometryFactory));
            } else if (envelopeWest < -180) {
                /**  west is between -360 and -180 & east is between -180 and 180*/
                Polygon[] polygons = {(Polygon)middle.intersection(geometry), (Polygon)toCanonicalLongitudes(left.intersection(geometry))};
                geometries.add(polygons[0]);
                geometries.add(polygons[1]);
                return new Tuple2<>(geometries, new MultiPolygon(polygons,geometryFactory));            }
        }
        return new Tuple2(geometries, geometry);
    }
}
