package io.arlas.server.model.response;


import org.geojson.FeatureCollection;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class TimedFeatureCollection extends FeatureCollection{
    public Long queryTime = null;
    public Long arlasTime = null;
}
