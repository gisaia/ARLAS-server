package io.arlas.server.model.response;

import java.util.Map;

import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.utils.GeoTypeMapper;
import io.arlas.server.utils.TimestampTypeMapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class Hit {

    public MD md;

    public Object data;

    public Hit() {}
    
    public Hit(CollectionReference collectionReference, Map<String,Object> source) throws ArlasException {
        data = source;
        md = new MD();
        if (collectionReference.params.idPath != null
                && source.get(collectionReference.params.idPath) != null) {
            md.id = "" + source.get(collectionReference.params.idPath);
        }
        if (collectionReference.params.centroidPath != null
                && source.get(collectionReference.params.centroidPath) != null) {
            Object m = source.get(collectionReference.params.centroidPath);
            md.centroid = GeoTypeMapper.getGeoJsonObject(m);
        }
        if (collectionReference.params.geometryPath != null
                && source.get(collectionReference.params.geometryPath) != null) {
            Object m = source.get(collectionReference.params.geometryPath);
            md.geometry = GeoTypeMapper.getGeoJsonObject(m);
        }
        if (collectionReference.params.timestampPath != null
                && source.get(collectionReference.params.timestampPath) != null) {
            Object t = source.get(collectionReference.params.timestampPath);
            if (collectionReference.params.custom_params != null){
                String f = collectionReference.params.custom_params.get(CollectionReference.TIMESTAMP_FORMAT);
                md.timestamp = TimestampTypeMapper.getTimestamp(t,f);
            }

        }
    }
}
