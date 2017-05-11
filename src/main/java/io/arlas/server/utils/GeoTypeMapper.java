package io.arlas.server.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.elasticsearch.common.geo.GeoPoint;
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.NotImplementedException;

public class GeoTypeMapper {
    
    private static ObjectMapper mapper = new ObjectMapper();
    private static ObjectReader reader = mapper.readerFor(GeoJsonObject.class);
    
    private static Logger LOGGER = LoggerFactory.getLogger(GeoTypeMapper.class);
    
    @SuppressWarnings("rawtypes")
    public static GeoJsonObject getGeoJsonObject(Object elasticsearchGeoField) throws ArlasException {
        GeoJsonObject geoObject = null;
        if(elasticsearchGeoField instanceof String) {
            //Standard lat/lon or geohash geo_point field
            GeoPoint geoPoint = new GeoPoint(elasticsearchGeoField.toString());
            geoObject = new Point(geoPoint.getLon(),geoPoint.getLat());
        } else if(elasticsearchGeoField instanceof ArrayList
                && ((ArrayList)elasticsearchGeoField).size()==2) {
            //Standard lon/lat array as geo_point field
            try {
                geoObject = new Point((Double)((ArrayList)elasticsearchGeoField).get(0),(Double)((ArrayList)elasticsearchGeoField).get(1));
            } catch (Exception e) {
                LOGGER.error("unable to parse geo_point from "+elasticsearchGeoField.getClass()+" :" + elasticsearchGeoField,e);
                throw new NotImplementedException("Not supported geo_point format found.");
            }
        } else if(elasticsearchGeoField instanceof HashMap) {
            if(((HashMap)elasticsearchGeoField).containsKey("type")) {
                //Standard GeoJSON object
                try {
                    geoObject = reader.readValue(mapper.writer().writeValueAsString(elasticsearchGeoField));
                } catch (IOException e) {
                    LOGGER.error("unable to parse geo_shape from "+elasticsearchGeoField.getClass()+" :" + elasticsearchGeoField,e);
                    throw new NotImplementedException("Not supported geo_point or geo_shape format found.");
                }
            } else if(((HashMap)elasticsearchGeoField).containsKey("lat") && 
                    ((HashMap)elasticsearchGeoField).containsKey("lon")) {
                try {
                    geoObject = new Point(((Double)((HashMap)elasticsearchGeoField).get("lon")),((Double)((HashMap)elasticsearchGeoField).get("lat")));
                } catch (Exception e) {
                    LOGGER.error("unable to parse geo_point from "+elasticsearchGeoField.getClass()+" :" + elasticsearchGeoField,e);
                    throw new NotImplementedException("Not supported geo_point format found.");
                }
            } else {
                LOGGER.error("unknwon geo_point or geo_shape from "+elasticsearchGeoField.getClass()+" :" + elasticsearchGeoField);
                throw new NotImplementedException("Not supported geo_point or geo_shape format found.");
            }
        } else {
            LOGGER.error("unknwon geo_point or geo_shape from "+elasticsearchGeoField.getClass()+" :" + elasticsearchGeoField);
            throw new NotImplementedException("Not supported geo_point or geo_shape format found.");
        }
        return geoObject;
    }

    public static void main(String[] args) {
        
    }

}
