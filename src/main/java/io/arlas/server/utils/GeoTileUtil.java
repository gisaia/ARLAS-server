package io.arlas.server.utils;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;
import org.apache.lucene.geo.Rectangle;
import org.elasticsearch.common.geo.GeoHashUtils;

public class GeoTileUtil {

    public static final String INVALID_GEOHASH = "Invalid geohash";
    public static BoundingBox getBoundingBox(final Tile tile) {
        return getBoundingBox(tile.getxTile(), tile.getyTile(), tile.getzTile());
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
        int xtile = (int)Math.floor( (lon + 180) / 360 * (1<<zoom) );
        if (xtile < 0)
            xtile=0;
        if (xtile >= (1<<zoom))
            xtile=((1<<zoom)-1);
        return xtile;
    }

    static int getYTile(final double lat, final double lon, final int zoom) {
        int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) ) ;
        if (ytile < 0)
            ytile=0;
        if (ytile >= (1<<zoom))
            ytile=((1<<zoom)-1);
        return ytile;
    }

    public static Tile getTile(final double lat, final double lon, final int zoom) throws ArlasException{
        return new Tile(getXTile(lat,lon,zoom),getYTile(lat,lon,zoom),zoom);
    }
}
