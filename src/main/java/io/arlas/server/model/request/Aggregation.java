package io.arlas.server.model.request;

public class Aggregation {
    public AggregationType type;
    public String field;
    public String interval;
    public String format;
    public String collectField;
    public String collectFct;
    public String order;
    public AggregationOn on;
    public String size;

    public Aggregation() {
    }
}
