package io.arlas.server.model.request;

public class Aggregation {
    public AggregationTypeEnum type;
    public String field;
    public Interval interval;
    public String format;
    public String collectField;
    public String collectFct;
    public AggregationOrderEnum order;
    public AggregationOnEnum on;
    public String size;

    public Aggregation() {
    }
}
