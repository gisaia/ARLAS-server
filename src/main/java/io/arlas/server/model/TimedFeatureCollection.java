package io.arlas.server.model;


import org.geojson.FeatureCollection;

public class TimedFeatureCollection extends FeatureCollection{
    public Long queryTime = null;
    public Long arlasTime = null;
}
