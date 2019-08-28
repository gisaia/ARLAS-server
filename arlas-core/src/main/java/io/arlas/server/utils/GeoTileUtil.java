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
import org.geojson.GeoJsonObject;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

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
            r = GeoHashUtils.bbox(geohash);
        } catch (Exception e) {
            throw new InvalidParameterException(INVALID_GEOHASH);
        }
        return new BoundingBox(r.maxLat, r.minLat, r.minLon, r.maxLon);
    }

    public static BoundingBox bboxIntersects(BoundingBox bbox, String bboxCorners) throws ArlasException {
        BoundingBox ret = null;
        if (bbox != null && bboxCorners != null) {
            // west, south, east, north
            double topBboxCorner = Double.parseDouble(bboxCorners.split(",")[3].trim());
            double leftBboxCorner = Double.parseDouble(bboxCorners.split(",")[0].trim());
            double bottomBboxCorner = Double.parseDouble(bboxCorners.split(",")[1].trim());
            double rightBboxCorner = Double.parseDouble(bboxCorners.split(",")[2].trim());
            if (leftBboxCorner < rightBboxCorner) {
                // If the bbox is in Paris region
                ret = new BoundingBox(Math.min(bbox.getNorth(), topBboxCorner),
                        Math.max(bbox.getSouth(), bottomBboxCorner),
                        Math.max(bbox.getWest(), leftBboxCorner),
                        Math.min(bbox.getEast(), rightBboxCorner));
                if (ret.getWest() > ret.getEast()) {
                    ret = null;
                }
            } else if (leftBboxCorner > rightBboxCorner) { // If the bbox is in Béring Strait
                // If the bbox intersects the tile twice
                if (bbox.getWest() < rightBboxCorner && bbox.getEast() > leftBboxCorner) {
                    throw new NotImplementedException(FluidSearch.NOT_SUPPORTED_BBOX_INTERSECTION);
                } else {
                    // If there is one intersection
                    ret = new BoundingBox(Math.min(bbox.getNorth(), topBboxCorner), Math.max(bbox.getSouth(), bottomBboxCorner), bbox.getWest(), bbox.getEast());
                    // if only leftBboxCorner is "lefter" than the east of the tile ==> (rightBboxCorner < west of bbox)
                    if (bbox.getEast() > leftBboxCorner) {
                        ret.setWest(Math.max(bbox.getWest(), leftBboxCorner));
                        // and we dont set the east which stays bbox.getEast()
                    } else if (bbox.getWest() < rightBboxCorner) {
                        // if only rightBboxCorner is "righter" than the west of tile ==> (leftBboxCorner > east of bbox)
                        ret.setEast(Math.min(bbox.getEast(), rightBboxCorner));
                        // and we dont set the west which stays bbox.getWest()
                    } else {
                        ret = null;
                    }
                }
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
                geometry = GeoTypeMapper.readWKT(geoJsonObject.toString());
            } else {
                GeoJsonReader reader = new GeoJsonReader();
                ObjectWriter writer = new ObjectMapper().writer();
                geometry = reader.read(writer.writeValueAsString(geoJsonObject));
            }
        }
        return geometry;
    }
}
