package io.arlas.server;

import org.geojson.Polygon;

public class Data {
    public String id;
    public String fullname;
    public DataParams params = new DataParams();
    public GeometryParams geo_params = new GeometryParams();

    public class DataParams {
        public String job;
        public Long startdate;
    }

    public class GeometryParams {
        public Polygon geometry;
        public String centroid;
    }
}


