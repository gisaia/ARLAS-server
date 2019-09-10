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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.arlas.server.core.FluidSearch;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.exceptions.NotImplementedException;
import io.arlas.server.managers.CollectionReferenceManager;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.enumerations.GeoTypeEnum;
import org.apache.lucene.geo.Rectangle;
import org.elasticsearch.common.geo.GeoHashUtils;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;

import java.util.ArrayList;
import java.util.List;

public class GeoTileUtil {

    public static final String INVALID_WKT_RANGE = "Invalid WKT geometry. Coordinates out of range";
    public static final String INVALID_WKT = "Invalid WKT geometry.";

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
            r = GeoHashUtils.bbox(geohash);
        } catch (Exception e) {
            throw new InvalidParameterException(INVALID_GEOHASH);
        }
        return new BoundingBox(r.maxLat, r.minLat, r.minLon, r.maxLon);
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
            if (queryGeometry.getGeometryType().equals("Polygon") || queryGeometry.getGeometryType().equals("MultiPolygon")) {
                for (int i = 0; i< queryGeometry.getNumGeometries(); i++) {
                    Geometry sousQueryGeometry = queryGeometry.getGeometryN(i);
                    Envelope sousQueryEnvelope = sousQueryGeometry.getEnvelopeInternal();
                    // Validity of the WKT is already checked in getValidGeoFilters
                    double queryWest = sousQueryEnvelope.getMinX();
                    double queryEast = sousQueryEnvelope.getMaxX();
                    List<Geometry> sousQueryGeometries = new ArrayList<>();
                    if (queryWest >= -180 && queryEast <= 180) {
                        /** Polygon longitudes are between -180 and 180*/
                        sousQueryGeometries.add(sousQueryGeometry);
                    } else if (queryWest>=180 || queryEast <= -180) {
                        /** Polygon longitudes are between -360 and -180  OR  180 and 360*/
                        sousQueryGeometry = GeoUtil.toCanonicalLongitudes(sousQueryGeometry);
                        sousQueryGeometries.add(sousQueryGeometry);
                    } else {
                        /** Polygon longitudes are between -360 and 180  OR  -180 and 360*/
                        Geometry middle = geometryFactory.toGeometry(new Envelope(-180, 180, -90, 90));
                        Geometry left = geometryFactory.toGeometry(new Envelope(-360, -180, -90, 90));
                        Geometry right = geometryFactory.toGeometry(new Envelope(180, 360, -90, 90));
                        Geometry middleIntersection = middle.intersection(sousQueryGeometry);
                        Geometry leftIntersection = left.intersection(sousQueryGeometry);
                        Geometry rightIntersection = right.intersection(sousQueryGeometry);
                        if (!middleIntersection.toString().equals("POLYGON EMPTY")) {
                            sousQueryGeometries.add(middleIntersection);
                        }
                        if (!rightIntersection.toString().equals("POLYGON EMPTY")) {
                            sousQueryGeometries.add(GeoUtil.toCanonicalLongitudes(rightIntersection));
                        }
                        if (!leftIntersection.toString().equals("POLYGON EMPTY")) {
                            sousQueryGeometries.add(GeoUtil.toCanonicalLongitudes(leftIntersection));
                        }
                    }
                    sousQueryGeometries.forEach(geoQuery -> {
                        Geometry intersectionGeometry = geoQuery.intersection(bboxGeometry);
                        if (!intersectionGeometry.toString().equals("POLYGON EMPTY")) {
                            if (intersectionGeometry.getGeometryType().equals("Polygon") || intersectionGeometry.getGeometryType().equals("MultiPolygon")) {
                                intersections.add(intersectionGeometry);
                            }
                        }
                    });
                }
                if (!intersections.isEmpty()) {
                    if (intersections.size() == 1) {
                        return intersections.get(0);
                    } else {
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
