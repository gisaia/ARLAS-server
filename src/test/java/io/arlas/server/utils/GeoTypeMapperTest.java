package io.arlas.server.utils;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.geojson.Point;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.arlas.server.exceptions.ArlasException;

public class GeoTypeMapperTest {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testGetGeoJsonObject() throws ArlasException, JsonProcessingException {
        String pointStringLatLon = "41.12,-71.34";
        String pointStringGeohash = "drm3btev3e86";
        ArrayList pointArray = new ArrayList();
        pointArray.add(-71.34);
        pointArray.add(41.12);
        HashMap pointMap = new HashMap();
        pointMap.put("lat", 41.12);
        pointMap.put("lon", -71.34);
        
        Point refPoint = new Point(-71.34,41.12);
        assertTrue(GeoTypeMapper.getGeoJsonObject(pointStringLatLon).equals(refPoint));
        assertTrue(GeoTypeMapper.getGeoJsonObject(pointStringGeohash) instanceof Point);
        assertTrue(GeoTypeMapper.getGeoJsonObject(pointArray).equals(refPoint));
        assertTrue(GeoTypeMapper.getGeoJsonObject(pointMap).equals(refPoint));
    }

}
