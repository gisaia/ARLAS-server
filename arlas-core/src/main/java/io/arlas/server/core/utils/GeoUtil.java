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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import cyclops.data.tuple.Tuple2;
import io.arlas.commons.exceptions.ArlasException;
import io.arlas.commons.exceptions.InvalidParameterException;
import org.geojson.GeoJsonObject;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GeoUtil {

    public static final String INVALID_WKT_RANGE = "Invalid geometry. Coordinates must be contained in the Envelope -360, 360, -180, 180";
    public static final String INVALID_WKT = "Invalid geometry ";
    public static final String POLYGON_EMPTY = "POLYGON EMPTY";

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final ObjectWriter writer = objectMapper.writer();
    public static final ObjectReader geojsonReader = objectMapper.readerFor(GeoJsonObject.class);
    public static final GeoJsonReader reader = new GeoJsonReader();

    public static Point getPoint(Double lon, Double lat) {
        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }

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
        WKTReader wkt = new WKTReader(geometryFactory);
        Geometry geom = null;
        try {
            // Geometry validity is already checked when parsing requests parameters
            geom = wkt.read(geometry);
        } catch (ParseException ex) {
            throw new InvalidParameterException(INVALID_WKT + ": " + ex.getMessage() + ".");
        }
        return geom;
    }

    public static void checkWKT(String wktString) throws InvalidParameterException {
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
     * Splits the CW oriented polygon into a list of polygons with longitudes between -180 and 180 and with a longitude extent <= 180
     * @param polygon CW oriented polygon
     * @return The list of polygons and a join of these polygons in a MultiPolygon as a Tuple
     * @throws ArlasException
     */
    public static Tuple2<List<Polygon>, Geometry> splitPolygon(Polygon polygon) throws ArlasException {
        Envelope envelope = polygon.getEnvelopeInternal();
        List<Polygon> geometries = new ArrayList<>();
        double envelopeEast = envelope.getMaxX();
        double envelopeWest = envelope.getMinX();
        Geometry middleWest = geometryFactory.toGeometry(new Envelope(-180, 0, -90, 90));
        Geometry middleEast = geometryFactory.toGeometry(new Envelope(0, 180, -90, 90));
        if (envelopeEast <= 180 && envelopeWest >= -180) {
            /** longitudes between -180 and 180**/
            if ((envelopeEast - envelopeWest) > 180) {
                geometries = Arrays.asList(middleWest.intersection(polygon), middleEast.intersection(polygon))
                        .stream().filter(g ->!isPolygonEmpty(g) && CheckParams.isPolygon(g)).map(g -> (Polygon)g).collect(Collectors.toList());
            } else {
                geometries.add(polygon);
            }
            return new Tuple2(geometries, polygon);
        } else {
            Geometry left = geometryFactory.toGeometry(new Envelope(-360, -180, -90, 90));
            Geometry right = geometryFactory.toGeometry(new Envelope(180, 360, -90, 90));
            if (envelopeWest >= 180 || envelopeEast <= -180) {
                /** longitudes between 180 and 360 OR longitudes between -360 and -180**/
                geometries.add((Polygon) toCanonicalLongitudes(polygon));
                return new Tuple2(geometries, polygon);
            } else if (envelopeEast > 180) {
                /**  west is between -180 and 180 & east is beyond 180*/
                geometries = Arrays.asList(middleWest.intersection(polygon), middleEast.intersection(polygon), toCanonicalLongitudes(right.intersection(polygon)))
                        .stream().filter(g ->!isPolygonEmpty(g) && CheckParams.isPolygon(g)).map(g -> (Polygon)g).collect(Collectors.toList());
                return new Tuple2<>(geometries, new MultiPolygon(geometries.stream().toArray(Polygon[]::new),geometryFactory));
            } else if (envelopeWest < -180) {
                /**  west is between -360 and -180 & east is between -180 and 180*/
                geometries = new ArrayList<>(Arrays.asList(middleWest.intersection(polygon), middleEast.intersection(polygon), toCanonicalLongitudes(left.intersection(polygon)))
                        .stream().filter(g ->!isPolygonEmpty(g) && CheckParams.isPolygon(g)).map(g -> (Polygon)g).collect(Collectors.toList()));
                return new Tuple2<>(geometries, new MultiPolygon(geometries.stream().toArray(Polygon[]::new),geometryFactory));
            }
        }
        return new Tuple2(geometries, polygon);
    }

    public static boolean isPolygonEmpty(Geometry geometry) {
       return POLYGON_EMPTY.equals(geometry.toString());
    }

    public static double[] getBbox(GeoJsonObject geoJsonObject) {
        try {
            Geometry geometry = reader.read(writer.writeValueAsString(geoJsonObject)).getEnvelope();

            double minX = 200;
            double maxX = 200;
            double minY = 200;
            double maxY = 200;
            for (Coordinate c : geometry.getCoordinates()) {
                minX = minX == 200 ? c.x : Math.min(minX, c.x);
                maxX = maxX == 200 ? c.x : Math.max(maxX, c.x);
                minY = minY == 200 ? c.y : Math.min(minY, c.y);
                maxY = maxY == 200 ? c.y : Math.max(maxY, c.y);
            }
            return new double[] { minX, minY, maxX, maxY};
        } catch (JsonProcessingException | ParseException e) {
        }
        return null;
    }

    public static Geometry toClockwise(final GeoJsonObject geojson) throws JsonProcessingException, ParseException {
        Geometry geometry = reader.read(writer.writeValueAsString(geojson));
        return toClockwise(geometry);
    }

    public static Geometry toClockwise(final Geometry geometry) {
        final GeometryFactory factory = geometry.getFactory();
        if (geometry instanceof MultiPolygon || geometry instanceof Polygon) {
            boolean isMultiPolygon = geometry instanceof MultiPolygon;
            int nbPolygon = geometry.getNumGeometries();

            final Polygon[] ps = new Polygon[nbPolygon];
            for (int i = 0; i < nbPolygon; i++) {
                final Polygon p = isMultiPolygon ? (Polygon) geometry.getGeometryN(i) : (Polygon) geometry;
                final LinearRing[] holes = new LinearRing[p.getNumInteriorRing()];
                LinearRing outer = p.getExteriorRing();
                if (Orientation.isCCW(outer.getCoordinateSequence())) {
                    outer = reverse(factory, p.getExteriorRing());
                }

                for (int t = 0, tt = p.getNumInteriorRing(); t < tt; t++) {
                    holes[t] = p.getInteriorRingN(t);
                    if (!Orientation.isCCW(holes[t].getCoordinateSequence())) {
                        holes[t] = reverse(factory, holes[t]);
                    }
                }
                ps[i] = factory.createPolygon(outer, holes);
            }

            Geometry reversed = isMultiPolygon ? factory.createMultiPolygon(ps) : ps[0];
            reversed.setSRID(geometry.getSRID());
            reversed.setUserData(geometry.getUserData());
            return reversed;

        } else if (geometry instanceof LinearRing) {
            LinearRing lr = (LinearRing) geometry;
            if (Orientation.isCCW(lr.getCoordinateSequence())) {
                lr = reverse(factory, lr);
            }
            return lr;

        } else {
            return geometry;
        }
    }

    public static Geometry toCounterClockwise(final Geometry geometry) {
        final GeometryFactory factory = geometry.getFactory();
        if (geometry instanceof MultiPolygon || geometry instanceof Polygon) {
            boolean isMultiPolygon = geometry instanceof MultiPolygon;
            int nbPolygon = geometry.getNumGeometries();

            final Polygon[] ps = new Polygon[nbPolygon];
            for (int i = 0; i < nbPolygon; i++) {
                final Polygon p = isMultiPolygon ? (Polygon) geometry.getGeometryN(i) : (Polygon) geometry;
                final LinearRing[] holes = new LinearRing[p.getNumInteriorRing()];
                LinearRing outer = p.getExteriorRing();
                if (!Orientation.isCCW(outer.getCoordinateSequence())) {
                    outer = reverse(factory, p.getExteriorRing());
                }

                for (int t = 0, tt = p.getNumInteriorRing(); t < tt; t++) {
                    holes[t] = p.getInteriorRingN(t);
                    if (Orientation.isCCW(holes[t].getCoordinateSequence())) {
                        holes[t] = reverse(factory, holes[t]);
                    }
                }
                ps[i] = factory.createPolygon(outer, holes);
            }

            Geometry reversed = isMultiPolygon ? factory.createMultiPolygon(ps) : ps[0];
            reversed.setSRID(geometry.getSRID());
            reversed.setUserData(geometry.getUserData());
            return reversed;

        } else if (geometry instanceof LinearRing) {
            LinearRing lr = (LinearRing) geometry;
            if (!Orientation.isCCW(lr.getCoordinateSequence())) {
                lr = reverse(factory, lr);
            }
            return lr;

        } else {
            return geometry;
        }
    }

    private static LinearRing reverse(final GeometryFactory factory, final LinearRing ring) {
        final CoordinateSequence cs = ring.getCoordinateSequence().copy();
        CoordinateSequences.reverse(cs);

        final LinearRing res = factory.createLinearRing(cs);
        res.setSRID(res.getSRID());
        res.setUserData(ring.getUserData());
        return res;
    }
}
