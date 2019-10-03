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
import com.fasterxml.jackson.databind.ObjectWriter;
import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.exceptions.InvalidParameterException;
import io.arlas.server.core.managers.CollectionReferenceManager;
import io.arlas.server.core.model.CollectionReference;
import io.arlas.server.core.model.enumerations.GeoTypeEnum;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.geom.*;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.io.GeohashUtils;
import org.locationtech.spatial4j.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class GeoTileUtil {


    public static final String INVALID_GEOHASH = "Invalid geohash";
    private static final GeometryFactory geoFactory = new GeometryFactory();
    public static BoundingBox getBoundingBox(final Tile tile) {
        return getBoundingBox(tile.getxTile(), tile.getyTile(), tile.getzTile());
    }

    public static Polygon toPolygon(BoundingBox bbox){
        return (Polygon)geoFactory.toGeometry(new Envelope(bbox.east, bbox.west, bbox.north, bbox.south));
    }

    public static BoundingBox getBoundingBox(final String geohash) throws ArlasException {
        Rectangle r;
        try {
            r = GeohashUtils.decodeBoundary(geohash, SpatialContext.GEO);
        } catch (Exception e) {
            throw new InvalidParameterException(INVALID_GEOHASH);
        }
        return new BoundingBox(r.getMaxY(), r.getMinY(), r.getMinX(), r.getMaxX());
    }

    /**
     *
     * @param bbox a BoundingBox object. The latitudes must be between -90 and 90 and the longitudes between -180 and 180 and the west<east
     * @param geoAsString a bbox string : 'west,south,east,north' or a WKT string
     * @return returns the intersection between the given `bbox` and the `geoAsString`
     * @throws ArlasException
     */
    public static Geometry bboxIntersects(BoundingBox bbox, String geoAsString) throws ArlasException {
        Geometry ret = null;
        if (bbox != null && geoAsString != null) {
            boolean isBBOX = CheckParams.isBboxMatch(geoAsString);
            if (isBBOX) {
                geoAsString = GeoUtil.bboxToWKT(geoAsString);
            }
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
            List<Coordinate> l = new ArrayList();
            l.add(new Coordinate(bbox.getWest(),bbox.getNorth()));
            l.add(new Coordinate(bbox.getEast(),bbox.getNorth()));
            l.add(new Coordinate(bbox.getEast(),bbox.getSouth()));
            l.add(new Coordinate(bbox.getWest(),bbox.getSouth()));
            l.add(new Coordinate(bbox.getWest(),bbox.getNorth()));
            Polygon bboxGeometry = new Polygon(new LinearRing(l.toArray(new Coordinate[l.size()]), new PrecisionModel(), 4326), null, geometryFactory);
            Geometry queryGeometry = GeoUtil.readWKT(geoAsString);
            List<Geometry> intersections = new ArrayList<>();
            /** For the case of Polygon and MultiPolygon that cross the dateline, we split it in order to obtain polygons with longitudes between -180 and 180 and therefore apply the intersection with the bboxGeometry **/
            if (queryGeometry.getGeometryType().equals("Polygon") || queryGeometry.getGeometryType().equals("MultiPolygon")) {
                for (int i = 0; i< queryGeometry.getNumGeometries(); i++) {
                    Geometry subGeometry = queryGeometry.getGeometryN(i);
                    // Validity of the WKT is already checked in getValidGeoFilters
                    List<Geometry> subGeometries = GeoUtil.splitGeometryOnDateline(subGeometry)._1();
                    subGeometries.forEach(geometry -> {
                        Geometry intersectionGeometry = geometry.intersection(bboxGeometry);
                        if (!intersectionGeometry.toString().equals("POLYGON EMPTY")) {
                            /** we only consider geometries that intersects the bbox as a Polygon**/
                            if (intersectionGeometry.getGeometryType().equals("Polygon")) {
                                intersections.add(intersectionGeometry);
                            }
                        }
                    });
                }
                if (!intersections.isEmpty()) {
                    if (intersections.size() == 1) {
                        return intersections.get(0);
                    } else {
                        /** in case the geometry intersects the bbox more than one time, we return a MultiPolygon*/
                        return new MultiPolygon(intersections.toArray(new Polygon[intersections.size()]), geometryFactory);
                    }
                }
            } else {
                Envelope queryEnveloppe = queryGeometry.getEnvelopeInternal();
                double queryWest = queryEnveloppe.getMinX();
                double queryEast = queryEnveloppe.getMaxX();
                if (queryWest>=180 || queryEast <= -180) {
                    queryGeometry = GeoUtil.toCanonicalLongitudes(queryGeometry);
                }
                Geometry intersectionGeometry = queryGeometry.intersection(bboxGeometry);
                return intersectionGeometry;
            }
        }
        return ret;
    }


    public static BoundingBox getBoundingBox(final int x, final int y, final int z) {
        double north = getLat(y, z);
        double south = getLat(y + 1, z);
        double west = getLon(x, z);
        double east = getLon(x + 1, z);
        return new BoundingBox(north, south, west, east);
    }

    static double getLon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    static double getLat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    static int getXTile(final double lat, final double lon, final int zoom) {
        int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
        if (xtile < 0)
            xtile = 0;
        if (xtile >= (1 << zoom))
            xtile = ((1 << zoom) - 1);
        return xtile;
    }

    static int getYTile(final double lat, final double lon, final int zoom) {
        int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
        if (ytile < 0)
            ytile = 0;
        if (ytile >= (1 << zoom))
            ytile = ((1 << zoom) - 1);
        return ytile;
    }

    public static Tile getTile(final double lat, final double lon, final int zoom) throws ArlasException {
        return new Tile(getXTile(lat, lon, zoom), getYTile(lat, lon, zoom), zoom);
    }

    public static Geometry getGeometryFromSource(Object source, CollectionReference collectionReference) throws ArlasException, JsonProcessingException, ParseException {
        CollectionReferenceManager.setCollectionGeometriesType(source, collectionReference);
        Geometry geometry = null;
        Object geoJsonObject = MapExplorer.getObjectFromPath(collectionReference.params.geometryPath, source);
        if (geoJsonObject != null) {
            if (collectionReference.params.getGeometryType().equals(GeoTypeEnum.WKT)) {
                geometry = GeoUtil.readWKT(geoJsonObject.toString());
            } else {
                GeoJsonReader reader = new GeoJsonReader();
                ObjectWriter writer = new ObjectMapper().writer();
                geometry = reader.read(writer.writeValueAsString(geoJsonObject));
            }
        }
        return geometry;
    }
}
